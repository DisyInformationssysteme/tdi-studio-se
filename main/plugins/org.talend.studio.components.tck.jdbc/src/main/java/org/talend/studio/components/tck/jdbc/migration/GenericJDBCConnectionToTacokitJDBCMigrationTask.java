package org.talend.studio.components.tck.jdbc.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.TacokitDatabaseConnection;
import org.talend.core.model.migration.AbstractItemMigrationTask;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.TacokitDatabaseConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.studio.Lookups;
import org.talend.sdk.component.studio.metadata.model.TaCoKitConfigurationModel.BuiltInKeys;

public class GenericJDBCConnectionToTacokitJDBCMigrationTask extends AbstractItemMigrationTask {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2023, 7, 11, 12, 0, 0);
        return gc.getTime();
    }

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.JDBC);
        toReturn.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        return toReturn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.migration.IProjectMigrationTask#execute(org.talend.core.model.general.Project,
     * org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        if (item instanceof DatabaseConnectionItem) {
            ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            DatabaseConnectionItem connectionItem = (DatabaseConnectionItem) item;
            DatabaseConnection connection = (DatabaseConnection) connectionItem.getConnection();
            String dbType = connection.getDatabaseType();
            if (dbType == null || !dbType.equals("JDBC")) {
                return ExecutionResult.NOTHING_TO_DO;
            }
            TacokitDatabaseConnection tacokitDatabaseConnection = ConnectionFactory.eINSTANCE.createTacokitDatabaseConnection();
            TacokitDatabaseConnectionItem tacokitDatabaseConnectionItem = PropertiesFactory.eINSTANCE
                    .createTacokitDatabaseConnectionItem();
            tacokitDatabaseConnectionItem.setConnection(tacokitDatabaseConnection);
            tacokitDatabaseConnectionItem.setFileExtension(item.getFileExtension());
            tacokitDatabaseConnectionItem.setParent(item.getParent());
            Property property = PropertiesFactory.eINSTANCE.createProperty();
            tacokitDatabaseConnectionItem.setProperty(property);

            property.setAuthor(item.getProperty().getAuthor());
            property.setCreationDate(item.getProperty().getCreationDate());
            property.setDescription(item.getProperty().getDescription());
            property.setDisplayName(item.getProperty().getDisplayName());
            property.setId(item.getProperty().getId());
            property.setItem(tacokitDatabaseConnectionItem);
            property.setLabel(item.getProperty().getLabel());
            property.setMaxInformationLevel(item.getProperty().getMaxInformationLevel());
            property.setModificationDate(item.getProperty().getModificationDate());
            property.setOldStatusCode(item.getProperty().getOldStatusCode());
            property.setPurpose(item.getProperty().getPurpose());
            property.setStatusCode(item.getProperty().getStatusCode());
            property.setVersion(item.getProperty().getVersion());
            property.getAdditionalProperties().addAll(EcoreUtil.copyAll(item.getProperty().getAdditionalProperties()));

            tacokitDatabaseConnection.getProperties().putAll(connection.getProperties());
            tacokitDatabaseConnection.setDbmsId(connection.getDbmsId());
            tacokitDatabaseConnection.setURL(connection.getURL());
            tacokitDatabaseConnection.setDatabaseType(connection.getDatabaseType());
            if (connection.isContextMode()) {
                tacokitDatabaseConnection.setDriverJarPath(connection.getDriverJarPath());
            } else {
                String driverPath = connection.getDriverJarPath();
                StringBuffer newPathSB = new StringBuffer();
                if (driverPath != null) {
                    String[] values = driverPath.split(";");
                    for (String v : values) {
                        if (newPathSB.length() > 0) {
                            newPathSB.append(";");
                        }
                        if (v.startsWith("\"") && v.endsWith("\"")) {
                            newPathSB.append(v);
                        } else {
                            newPathSB.append("\"").append(v).append("\"");
                        }
                    }
                } 
                tacokitDatabaseConnection.setDriverJarPath(newPathSB.toString());
            }
            tacokitDatabaseConnection.setDriverClass(connection.getDriverClass());
            tacokitDatabaseConnection.setUsername(connection.getUsername());
            tacokitDatabaseConnection.setPassword(connection.getPassword());
            tacokitDatabaseConnection.setProductId(connection.getProductId());
            ConfigTypeNode configNode = Lookups.taCoKitCache().findDatastoreConfigTypeNodeByName("JDBC");
            tacokitDatabaseConnection.getProperties().put(BuiltInKeys.TACOKIT_CONFIG_ID, configNode.getId());
            tacokitDatabaseConnection.getProperties().put(BuiltInKeys.TACOKIT_CONFIG_PARENT_ID, configNode.getParentId());

            tacokitDatabaseConnection.setCdcConns(EcoreUtil.copy(connection.getCdcConns()));
            tacokitDatabaseConnection.setCdcTypeMode(connection.getCdcTypeMode());
            tacokitDatabaseConnection.setContextId(connection.getContextId());
            tacokitDatabaseConnection.setContextMode(connection.isContextMode());
            tacokitDatabaseConnection.setContextName(connection.getContextName());
            tacokitDatabaseConnection.setAdditionalParams(connection.getAdditionalParams());
            tacokitDatabaseConnection.setDatasourceName(connection.getDatasourceName());
            tacokitDatabaseConnection.setDBRootPath(connection.getDBRootPath());
            tacokitDatabaseConnection.setDbVersionString(connection.getDbVersionString());
            tacokitDatabaseConnection.setDivergency(connection.isDivergency());
            tacokitDatabaseConnection.setFileFieldName(connection.getFileFieldName());
            
            // Port is no used for JDBC since it use URL to connect to db. comment this line for TUP-40224
//            tacokitDatabaseConnection.setPort(connection.getPort());
            tacokitDatabaseConnection.setUiSchema(connection.getUiSchema());
            tacokitDatabaseConnection.setServerName(connection.getServerName());
            tacokitDatabaseConnection.setSID(connection.getSID());
            tacokitDatabaseConnection.setComment(connection.getComment());

            tacokitDatabaseConnection.setId(connection.getId());
            tacokitDatabaseConnection.setLabel(connection.getLabel());
            tacokitDatabaseConnection.setNullChar(connection.getNullChar());
            tacokitDatabaseConnection.setProductId(connection.getProductId());
            tacokitDatabaseConnection.setSqlSynthax(connection.getSqlSynthax());
            tacokitDatabaseConnection.setStandardSQL(connection.isStandardSQL());
            tacokitDatabaseConnection.setStringQuote(connection.getStringQuote());
            tacokitDatabaseConnection.setSynchronised(connection.isSynchronised());
            tacokitDatabaseConnection.setSystemSQL(connection.isSystemSQL());
            tacokitDatabaseConnection.setVersion(connection.getVersion());
            tacokitDatabaseConnection.setReadOnly(connection.isReadOnly());
            tacokitDatabaseConnection.setName(connection.getName());
            tacokitDatabaseConnection.setNamespace(EcoreUtil.copy(connection.getNamespace()));

            tacokitDatabaseConnection.setIsCaseSensitive(connection.isIsCaseSensitive());
            tacokitDatabaseConnection.setMachine(EcoreUtil.copy(connection.getMachine()));
            tacokitDatabaseConnection.setPathname(connection.getPathname());
            //tacokitDatabaseConnection.setPort(connection.getPort());
            tacokitDatabaseConnection.setQueries(EcoreUtil.copy(connection.getQueries()));
            tacokitDatabaseConnection.setStereotype(EcoreUtil.copy(connection.getStereotype()));
            tacokitDatabaseConnection.setSupportNLS(connection.isSupportNLS());

            tacokitDatabaseConnection.getDataPackage().addAll(EcoreUtil.copyAll(connection.getDataPackage()));
            tacokitDatabaseConnection.getConstraint().addAll(EcoreUtil.copyAll(connection.getConstraint()));
            tacokitDatabaseConnection.getChangeRequest().addAll(EcoreUtil.copyAll(connection.getChangeRequest()));
            tacokitDatabaseConnection.getClientDependency().addAll(EcoreUtil.copyAll(connection.getClientDependency()));
            tacokitDatabaseConnection.getDataManager().addAll(EcoreUtil.copyAll(connection.getDataManager()));
            tacokitDatabaseConnection.getDasdlProperty().addAll(EcoreUtil.copyAll(connection.getDasdlProperty()));
            tacokitDatabaseConnection.getDeployedSoftwareSystem().addAll(EcoreUtil.copyAll(connection.getDeployedSoftwareSystem()));
            tacokitDatabaseConnection.getDescription().addAll(EcoreUtil.copyAll(connection.getDescription()));
            tacokitDatabaseConnection.getDocument().addAll(EcoreUtil.copyAll(connection.getDocument()));
            tacokitDatabaseConnection.getElementNode().addAll(EcoreUtil.copyAll(connection.getElementNode()));
            tacokitDatabaseConnection.getImportedElement().addAll(EcoreUtil.copyAll(connection.getImportedElement()));
            tacokitDatabaseConnection.getImporter().addAll(EcoreUtil.copyAll(connection.getImporter()));
            tacokitDatabaseConnection.getMeasurement().addAll(EcoreUtil.copyAll(connection.getMeasurement()));
            tacokitDatabaseConnection.getOwnedElement().addAll(EcoreUtil.copyAll(connection.getOwnedElement()));
            tacokitDatabaseConnection.getParameters().addAll(EcoreUtil.copyAll(connection.getParameters()));
            tacokitDatabaseConnection.getRenderedObject().addAll(EcoreUtil.copyAll(connection.getRenderedObject()));
            tacokitDatabaseConnection.getResourceConnection().addAll(EcoreUtil.copyAll(connection.getResourceConnection()));
            tacokitDatabaseConnection.getResponsibleParty().addAll(EcoreUtil.copyAll(connection.getResponsibleParty()));
            tacokitDatabaseConnection.getSupplierDependency().addAll(EcoreUtil.copyAll(connection.getSupplierDependency()));
            tacokitDatabaseConnection.getTaggedValue().addAll(EcoreUtil.copyAll(connection.getTaggedValue()));
            tacokitDatabaseConnection.getVocabularyElement().addAll(EcoreUtil.copyAll(connection.getVocabularyElement()));
            
            tacokitDatabaseConnection.setEnableDBType(false);

            if (connection.isSetSQLMode()) {
                tacokitDatabaseConnection.setSQLMode(connection.isSQLMode());
            } else {
                // set true by default as it's only used actually for teradata.
                // should be modified if default value is changed later.
                tacokitDatabaseConnection.setSQLMode(true);
            }
            try {
                IRepositoryViewObject object = factory.getSpecificVersion(item.getProperty().getId(),
                        item.getProperty().getVersion(), true);
                factory.deleteObjectPhysical(object);
                factory.create(tacokitDatabaseConnectionItem, new Path(item.getState().getPath()), true);
                return ExecutionResult.SUCCESS_WITH_ALERT;
            } catch (Exception e) {
                ExceptionHandler.process(e); 
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.NOTHING_TO_DO;

    }
}
