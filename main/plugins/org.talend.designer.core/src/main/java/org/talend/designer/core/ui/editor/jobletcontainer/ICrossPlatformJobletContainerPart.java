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
package org.talend.designer.core.ui.editor.jobletcontainer;

import java.util.List;

import org.talend.designer.core.ui.editor.nodecontainer.ICrossPlatformNodeContainerPart;

public interface ICrossPlatformJobletContainerPart extends ICrossPlatformNodeContainerPart {

    @Override
    default public List getCrossPlatformModelChildren() {
        return ((JobletContainer) this.getCrossPlatformModel()).getElements();
    }

}
