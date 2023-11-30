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
package org.talend.designer.unifiedcomponent.unifier.datapreptacokit;

import org.talend.designer.unifiedcomponent.i18n.Messages;
import org.talend.designer.unifiedcomponent.unifier.AbstractComponentsUnifier;


public class DataprepTacokitComponentsUnifier extends AbstractComponentsUnifier {
    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.unifiedcomponent.unifier.IComponentsUnifier#getDatabase()
     */
    @Override
    public String getDisplayName() {
        return Messages.getString("DataprepComponentsUnifier.lbl.Cloud");
    }
}
