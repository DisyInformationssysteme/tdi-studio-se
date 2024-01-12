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
package org.talend.designer.core.ui.editor.nodes;

import org.eclipse.draw2d.IFigure;

public class CrossPlatformSwtFigureProxy implements ICrossPlatformFigure {

    private IFigure figure;

    public CrossPlatformSwtFigureProxy(IFigure figure) {
        this.figure = figure;
    }

    public IFigure getFigure() {
        return this.figure;
    }

}
