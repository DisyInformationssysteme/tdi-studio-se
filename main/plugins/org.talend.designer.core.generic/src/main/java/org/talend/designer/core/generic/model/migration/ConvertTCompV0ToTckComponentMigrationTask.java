// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.generic.model.migration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.utils.ParameterUtilTool;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.utils.security.StudioEncryption;

public abstract class ConvertTCompV0ToTckComponentMigrationTask extends AbstractJobMigrationTask {

    static class TckMigrationModel {
        String oldComponentName;
        String newComponentName;
        
        String oldPath;
        String newPath;
        String fieldType;
    }
    
    @Override
    public ExecutionResult execute(final Item item) {
        final ProcessType processType = getProcessType(item);
        final ComponentCategory category = ComponentCategory.getComponentCategoryFromItem(item);
        final Map<String, List<TckMigrationModel>> props = getMigrationInfoFromFile();
        final IComponentConversion conversion = new IComponentConversion() {

            @Override
            public void transform(final NodeType nodeType) {
                if (nodeType == null || props == null) {
                    return;
                }
                
                final String currComponentName = nodeType.getComponentName();
                final List<TckMigrationModel> infos = props.get(currComponentName);
                final String newComponentName = infos.get(0).newComponentName;
                
                //update component node name to new one
                nodeType.setComponentName(newComponentName);
                nodeType.setComponentVersion("1");
                
                //get the new tck component define model
                //final IComponent component = ComponentsFactoryProvider.getInstance().get(newComponentName, category.getName());
                
                //if use label format in current component instance, and label is a defined as a var like tcompv0 model "__sql__", 
                //the code below for that:
                String label = ParameterUtilTool.getParameterValue(nodeType, "LABEL");
                if (label != null) {
                    ElementParameterType paraType = ParameterUtilTool.findParameterType(nodeType, label.replaceAll("_", ""));
                    if (paraType != null) {
                        ParameterUtilTool.findParameterType(nodeType, "LABEL").setValue(paraType.getValue());
                    }
                }
                
                ElementParameterType tcompV0PropertiesElement = ParameterUtilTool.findParameterType(nodeType, "PROPERTIES");
                //final ComponentProperties compProperties = ComponentsUtils.getComponentProperties(currComponentName);
                final String jsonProperties = tcompV0PropertiesElement.getValue();
                //TODO make sure it go through tcompv0 self migration, but tcompv0 jdbc have two migrations which all no need to be used here, 
                //so may make sure it in future, not now
                final ComponentProperties compProperties = Properties.Helper.fromSerializedPersistent(jsonProperties, ComponentProperties.class).object;
                
                final TalendFileFactory fileFact = TalendFileFactory.eINSTANCE;
                
                //TODO tcompv0/javajet component force user to use double quote to wrap value most time, but tck one not do that, even expect not use double quote wrapper, need to consider that 
                for(TckMigrationModel model : infos) {
                    if(model.fieldType == null || model.fieldType.isEmpty()) {
                        //field="TECHNICAL", and oldPath is value directly, so use it directly
                        ElementParameterType ept = ParameterUtilTool.createParameterType("TECHNICAL", model.newPath, model.oldPath);
                        ParameterUtilTool.addParameterType(nodeType, ept);
                    } else {
                        if("COMPONENT_LIST".equals(model.fieldType)) {
                            final Enum<?> referenceType = Enum.class.cast(Property.class.cast(compProperties.getProperty(model.oldPath + ".referenceType")).getStoredValue());
                            ComponentUtilities.addNodeProperty(nodeType, "USE_EXISTING_CONNECTION", "CHECK");
                            ComponentUtilities.addNodeProperty(nodeType, model.newPath, "COMPONENT_LIST");
                            if(referenceType == null || "THIS_COMPONENT".equals(referenceType.name())) {
                                ComponentUtilities.setNodeValue(nodeType, "USE_EXISTING_CONNECTION", "false");
                            } else {
                                ComponentUtilities.setNodeValue(nodeType, "USE_EXISTING_CONNECTION", "true");
                                final Object value = Property.class.cast(compProperties.getProperty(model.oldPath + ".componentInstanceId")).getStoredValue();
                                ComponentUtilities.setNodeValue(nodeType, model.newPath, value == null ? null : String.valueOf(value));
                            }
                        } else if("TABLE".equals(model.fieldType)) {
                            java.util.List<ElementValueType> elementValues = new ArrayList<ElementValueType>();
                            String[] tableColumnMappings = model.oldPath.split(";");
                            for(String mapping : tableColumnMappings) {
                                String[] oldPathAndNewPath = mapping.split("=");
                                String oldPath = oldPathAndNewPath[0];
                                String newPath = oldPathAndNewPath[1];
                                
                                Property property = Property.class.cast(compProperties.getProperty(oldPath));
                                List value = List.class.cast(property.getStoredValue());//no password, so ok
                                if(value == null) {
                                    //any list is null, break directly as tcompv0 table store every column as list, their size should be the same
                                    elementValues.clear();
                                    break;
                                }
                                for(Object item : value) {
                                    ElementValueType elementValue = fileFact.createElementValueType();
                                    elementValue.setElementRef(model.newPath + "[]."+ newPath);
                                    elementValue.setValue(item == null ? null : String.valueOf(item));
                                    elementValues.add(elementValue);
                                }
                            }
                            ComponentUtilities.addNodeProperty(nodeType, model.newPath, "TABLE");
                            ComponentUtilities.setNodeProperty(nodeType, model.newPath, elementValues);
                        } else if("PASSWORD".equals(model.fieldType)) {
                            Property property = Property.class.cast(compProperties.getProperty(model.oldPath));
                            String value = String.class.cast(property.getStoredValue());
                            if(value != null) {//TODO check if user not set password(default is "\"\""), or empty string as user remove default one
                                if(value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
                                    //need to encrypt with double quote
                                    value = StudioEncryption.getStudioEncryption(StudioEncryption.EncryptionKeyName.SYSTEM).encrypt(value);
                                } else {//a var like context.pwd, no need to encrypt
                                    
                                }
                            }
                            ElementParameterType ept = ParameterUtilTool.createParameterType(model.fieldType, model.newPath, value);
                            ParameterUtilTool.addParameterType(nodeType, ept);
                        } else {
                            Property property = Property.class.cast(compProperties.getProperty(model.oldPath));
                            Object value = property.getStoredValue();
                            //enum's tostring to call name(), so ok
                            ElementParameterType ept = ParameterUtilTool.createParameterType(model.fieldType, model.newPath, value == null ? null : String.valueOf(value));
                            ParameterUtilTool.addParameterType(nodeType, ept);
                        }
                    }
                }
                
                // Migrate schemas : the reverse action with the one in NewComponentFrameworkMigrationTask
                final String uniqueName = ParameterUtilTool.getParameterValue(nodeType, "UNIQUE_NAME");
                final EList<MetadataType> metadatas = nodeType.getMetadata();
                int indexOfFlow = -1;
                for (int i=0;i<metadatas.size();i++) {
                    final MetadataType metadataType = metadatas.get(i);
                    
                    if("FLOW".equals(metadataType.getConnector()) && uniqueName.equals(metadataType.getName())) {
                        indexOfFlow = i;
                    } if("MAIN".equals(metadataType.getConnector()) && "MAIN".equals(metadataType.getName())) {//tcompV0 use "MAIN" to match connection default
                        metadataType.setConnector("FLOW");
                        metadataType.setName(uniqueName);
                        
                        for (Object connectionObj : processType.getConnection()) {
                            if (connectionObj instanceof ConnectionType) {
                                ConnectionType connectionType = (ConnectionType) connectionObj;
                                if (connectionType.getSource().equals(uniqueName)
                                        && connectionType.getConnectorName().equals("MAIN")) {
                                    connectionType.setConnectorName("FLOW");
                                    connectionType.setMetaname(uniqueName);
                                }
                            }
                        }
                    } else if("REJECT".equals(metadataType.getConnector()) && "REJECT".equals(metadataType.getName())) {
                        //TODO tck jdbc connector special? check it
                        metadataType.setConnector("reject");
                        metadataType.setName("reject");
                        
                        for (Object connectionObj : processType.getConnection()) {
                            if (connectionObj instanceof ConnectionType) {
                                ConnectionType connectionType = (ConnectionType) connectionObj;
                                if (connectionType.getSource().equals(uniqueName)
                                        && connectionType.getConnectorName().equals("REJECT")) {
                                    connectionType.setConnectorName("reject");
                                    connectionType.setMetaname("reject");
                                }
                            }
                        }
                    }
                }
                if(indexOfFlow > -1) { 
                    metadatas.remove(indexOfFlow);
                }
                
                //remove tcompv0 PROPERTIES element at last
                ParameterUtilTool.removeParameterType(nodeType, tcompV0PropertiesElement);
                
                //no need to change the unique name as it use unify name like "tDBInput_1", and tcompv0's node's metadata(metadata name/connection name/schema) in item match the connection by the unique name,
                //so no need to migrate the metadata/connection line from tcompv0 to tck, it should work.
            }
        };

        if (processType != null) {
            boolean modified = false;
            for (Object obj : processType.getNode()) {
                if (obj != null && obj instanceof NodeType) {
                    String componentName = ((NodeType) obj).getComponentName();
                    if (props.get(componentName) == null) {
                        continue;
                    }
                    IComponentFilter filter = new NameComponentFilter(componentName);
                    modified = ModifyComponentsAction.searchAndModify((NodeType) obj, filter,
                            Arrays.<IComponentConversion> asList(conversion))
                            || modified;
                }
            }
            if (modified) {
                try {
                    ProxyRepositoryFactory.getInstance().save(item, true);
                    return ExecutionResult.SUCCESS_NO_ALERT;
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                    return ExecutionResult.FAILURE;
                }
            }
        }
        return ExecutionResult.NOTHING_TO_DO;
    }

    abstract protected String getMigrationFile();
    
    private Pattern pattern = Pattern.compile("(t?\\w++)(\\.([a-zA-Z_.]++))?=([^#]++)(#([a-zA-Z_]++))?");

    private Map<String, List<TckMigrationModel>> getMigrationInfoFromFile() {
        Map<String, List<TckMigrationModel>> result = new HashMap<>();
        try(InputStream in = getClass().getResourceAsStream(getMigrationFile());BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"))) {
            List<TckMigrationModel> modelList = new ArrayList<>();
            
            String oldComponentName = null;
            
            String line = null;
            while(true) {
                if((line = br.readLine())==null) {
                    if(oldComponentName!=null) {
                        result.put(oldComponentName, modelList);
                    }
                    break;
                }
                
                if(line.trim().isEmpty()) {//next component
                    if(oldComponentName!=null) {
                        result.put(oldComponentName, modelList);
                        modelList = new ArrayList<>();
                    }
                    continue;
                }
                
                Matcher matcher = pattern.matcher(line);
                if(matcher.find()) {
                    final TckMigrationModel model = new TckMigrationModel();
                    model.newComponentName = matcher.group(1);
                    model.newPath = matcher.group(3);
                    if(model.newPath == null || model.newPath.isEmpty()) {
                        oldComponentName = matcher.group(4);
                        model.oldComponentName = oldComponentName;
                    } else {
                        model.oldPath = matcher.group(4);
                        model.fieldType = matcher.group(6);
                        
                        modelList.add(model);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private ElementParameterType getParameterType(NodeType node, String paramName) {
        return ParameterUtilTool.findParameterType(node, paramName);
    }

}
