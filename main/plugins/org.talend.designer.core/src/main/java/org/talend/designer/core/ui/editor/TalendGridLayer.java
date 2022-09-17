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
package org.talend.designer.core.ui.editor;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FreeformFigure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.swt.graphics.Color;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.runtime.service.ITalendThemeService;
import org.talend.commons.ui.gmf.draw2d.AnimatableZoomManager;

/**
 * Grid that will be used for the designer. (modification of the default grid to have black points)
 *
 * $Id: TalendGridLayer.java 7038 2007-11-15 14:05:48Z plegall $
 *
 */
public class TalendGridLayer extends GridLayer {

    public static final Color GRID_COLOR = ColorConstants.black;

    private static final Color GRID_COLOR_1 = new Color(131, 131, 131);

    private static final Color GRID_COLOR_2 = new Color(255, 255, 255);

    private static ITalendThemeService themeServ;

    public TalendGridLayer() {
        super();
        setForegroundColor(ColorConstants.red);
    }

    private static ITalendThemeService getTheme() {
        if (themeServ == null) {
            themeServ = ITalendThemeService.get();
        }
        return themeServ;
    }

    private Color getThemeColor(String prop) {
        ITalendThemeService theme = getTheme();
        if (theme == null) {
            return null;
        }
        return (Color) theme.getGlobalThemeColor(prop);
    }

    private Color getColor1() {
        Color themeColor = getThemeColor("DESIGNER_COLOR_A");
        if (themeColor == null) {
            themeColor = GRID_COLOR_1;
        }
        return themeColor;
    }

    private Color getColor2() {
        Color themeColor = getThemeColor("DESIGNER_COLOR_B");
        if (themeColor == null) {
            themeColor = GRID_COLOR_2;
        }
        return themeColor;
    }

    private int getColorAlpha() {
        final int defaultAlpha = 30;
        ITalendThemeService theme = getTheme();
        if (theme == null) {
            return defaultAlpha;
        }
        String alpha = theme.getGlobalThemeProp("DESIGNER_COLOR_ALPHA");
        if (StringUtils.isBlank(alpha)) {
            return defaultAlpha;
        }
        try {
            return Integer.valueOf(alpha);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return defaultAlpha;
    }

    @Override
    protected void paintGrid(Graphics g) {
        paintGrid(g, this, origin, gridX, gridY);
    }

    protected void paintGrid(Graphics g, IFigure f, org.eclipse.draw2d.geometry.Point origin, int distanceX, int distanceY) {
        FreeformFigure ff = (FreeformFigure) this.getParent();
        Rectangle clientArea = getClientArea();
        Rectangle bounds = ff.getFreeformExtent().getCopy();
        bounds.union(clientArea.x, clientArea.y, clientArea.width * AnimatableZoomManager.currentZoom,
                clientArea.height * AnimatableZoomManager.currentZoom);
        ff.setFreeformBounds(bounds);

        Rectangle clip = g.getClip(Rectangle.SINGLETON);
        if (distanceX > 0 && distanceY > 0) {
            if (origin.x >= clip.x) {
                while (origin.x - distanceX >= clip.x) {
                    origin.x -= distanceX;
                }
            } else {
                while (origin.x < clip.x) {
                    origin.x += distanceX;
                }
            }
            if (origin.y >= clip.y) {
                while (origin.y - distanceY >= clip.y) {
                    origin.y -= distanceY;
                }
            } else {
                while (origin.y < clip.y) {
                    origin.y += distanceY;
                }
            }
            g.setAlpha(getColorAlpha());
            for (int i = origin.x - distanceX; i < clip.x + clip.width; i += distanceX) {
                for (int j = origin.y - distanceY; j < clip.y + clip.height; j += distanceY) {
//                    g.drawPoint(i, j);
                    int re = Math.abs(i - j);
                    if (re / distanceY % 2 == 0) {
                        g.setBackgroundColor(getColor1());
//                        g.drawImage(ImageProvider.getImage(EImage.CHESS_GRAY), i, j);
                    } else {
                        g.setBackgroundColor(getColor2());
                    }
                    g.fillRectangle(i, j, 32, 32);
                }
            }
        }
    }
}
