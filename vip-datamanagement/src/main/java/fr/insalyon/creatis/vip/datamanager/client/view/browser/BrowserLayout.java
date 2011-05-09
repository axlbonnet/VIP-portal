/* Copyright CNRS-CREATIS
 *
 * Rafael Silva
 * rafael.silva@creatis.insa-lyon.fr
 * http://www.creatis.insa-lyon.fr/~silva
 *
 * This software is a grid-enabled data-driven workflow manager and editor.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
package fr.insalyon.creatis.vip.datamanager.client.view.browser;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.NamedFrame;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.CellDoubleClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import fr.insalyon.creatis.vip.common.client.view.Context;
import fr.insalyon.creatis.vip.common.client.view.FieldUtil;
import fr.insalyon.creatis.vip.datamanager.client.DataManagerConstants;
import fr.insalyon.creatis.vip.datamanager.client.bean.Data;
import fr.insalyon.creatis.vip.datamanager.client.rpc.FileCatalogService;
import fr.insalyon.creatis.vip.datamanager.client.rpc.FileCatalogServiceAsync;
import fr.insalyon.creatis.vip.datamanager.client.view.operation.OperationLayout;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael Silva
 */
public class BrowserLayout extends VLayout {

    private static BrowserLayout instance;
    private BrowserToolStrip toolStrip;
    private ListGrid grid;

    public static BrowserLayout getInstance() {
        if (instance == null) {
            instance = new BrowserLayout();
        }
        return instance;
    }

    private BrowserLayout() {

        initComplete(this);
        this.setWidth100();
        this.setHeight100();
        this.setOverflow(Overflow.AUTO);
        this.setShowResizeBar(true);

        configureGrid();

        toolStrip = new BrowserToolStrip();
        this.addMember(toolStrip);
        this.addMember(grid);

        loadData(DataManagerConstants.ROOT, false);

        NamedFrame frame = new NamedFrame("uploadTarget");
        frame.setVisible(false);
        frame.setHeight("1px");
        frame.setWidth("1px");
        this.addMember(frame);
    }

    private void configureGrid() {
        grid = new ListGrid();
        grid.setWidth100();
        grid.setHeight100();
        grid.setShowAllRecords(false);
        grid.setShowEmptyMessage(true);
        grid.setEmptyMessage("<br>No data available.");
        grid.setSelectionType(SelectionStyle.SIMPLE);
        grid.setSelectionAppearance(SelectionAppearance.CHECKBOX);

        ListGridField icoField = FieldUtil.getIconGridField("icon");
        ListGridField nameField = new ListGridField("name", "Name");

        grid.setFields(icoField, nameField);
        grid.setSortField("icon");
        grid.setSortDirection(SortDirection.DESCENDING);

        grid.addCellDoubleClickHandler(new CellDoubleClickHandler() {

            public void onCellDoubleClick(CellDoubleClickEvent event) {
                String type = event.getRecord().getAttributeAsString("icon");
                if (type.contains("folder")) {
                    String name = event.getRecord().getAttributeAsString("name");
                    String path = toolStrip.getPath() + "/" + name;
                    loadData(path, false);
                }
            }
        });
    }

    public void loadData(final String path, boolean refresh) {

        if (!path.equals(DataManagerConstants.ROOT)) {
            FileCatalogServiceAsync service = FileCatalogService.Util.getInstance();
            AsyncCallback<List<Data>> callback = new AsyncCallback<List<Data>>() {

                public void onFailure(Throwable caught) {
                    SC.warn("Error executing get files list: " + caught.getMessage());
                }

                public void onSuccess(List<Data> result) {
                    if (result != null) {
                        List<DataRecord> dataList = new ArrayList<DataRecord>();
                        for (Data d : result) {
                            dataList.add(new DataRecord(
                                    d.getType().toLowerCase(), d.getName()));
                        }
                        toolStrip.setPath(path);
                        grid.setData(dataList.toArray(new DataRecord[]{}));

                    } else {
                        SC.warn("Unable to get list of files.");
                    }
                }
            };
            Context context = Context.getInstance();
            service.listDir(context.getUser(), context.getProxyFileName(), path, refresh, callback);

        } else {
            toolStrip.setPath(path);
            grid.setData(
                    new DataRecord[]{
                        new DataRecord("folder", DataManagerConstants.USERS_HOME),
                        new DataRecord("folder", DataManagerConstants.PUBLIC_HOME),
                        new DataRecord("folder", DataManagerConstants.GROUPS_HOME),
                        new DataRecord("folder", DataManagerConstants.ACTIVITIES_HOME),
                        new DataRecord("folder", DataManagerConstants.WORKFLOWS_HOME),
                        new DataRecord("folder", DataManagerConstants.CREATIS_HOME)
                    });
        }
    }
    
    public ListGridRecord[] getGridSelection() {
        return grid.getSelection();
    }

    public void uploadComplete(String fileName) {
        OperationLayout.getInstance().loadData();
    }

    private native void initComplete(BrowserLayout upload) /*-{
    $wnd.uploadComplete = function (fileName) {
    upload.@fr.insalyon.creatis.vip.datamanager.client.view.browser.BrowserLayout::uploadComplete(Ljava/lang/String;)(fileName);
    };
    }-*/;
}