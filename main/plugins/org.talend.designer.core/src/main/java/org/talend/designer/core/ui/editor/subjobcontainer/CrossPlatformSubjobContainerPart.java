// ============================================================================
//
// Copyright (C) 2006-2023 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.subjobcontainer;

import java.util.List;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class CrossPlatformSubjobContainerPart extends AbsCrossPlatformEditPart implements ICrossPlatformSubjobContainerPart {

    public CrossPlatformSubjobContainerPart(Object model) {
        super(model);
    }

    @Override
    public List getCrossPlatformModelChildren() {
        return ICrossPlatformSubjobContainerPart.super.getCrossPlatformModelChildren();
    }

    @Override
    public List getCrossPlatformModelSourceConnections() {
        return ICrossPlatformSubjobContainerPart.super.getCrossPlatformModelSourceConnections();
    }

}
