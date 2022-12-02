package org.mmarini.routes.swing;

import org.mmarini.routes.model2.ByDistanceBuilder;
import org.mmarini.routes.model2.BypassesBuilder;
import org.mmarini.routes.model2.ConnectionBuilder;
import org.mmarini.routes.model2.StarBuilder;

import javax.swing.*;

public class ConnectionsPane extends Box {
    private final JRadioButton starBuilderBtn;
    private final JRadioButton nearBuilderBtn;
    private final JRadioButton bypassBuilderBtn;

    public ConnectionsPane() {
        super(BoxLayout.PAGE_AXIS);
        this.starBuilderBtn = new JRadioButton(Messages.getString("ConnectionsPane.starBuilderBtn.label"));
        this.nearBuilderBtn = new JRadioButton(Messages.getString("ConnectionsPane.nearBuilderBtn.label"));
        this.bypassBuilderBtn = new JRadioButton(Messages.getString("ConnectionsPane.bypassBuilderBtn.label"));

        init();
        createContent();
    }

    private void createContent() {
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder(Messages.getString("ConnectionsPane.typePane.title"))); //$NON-NLS-1$
        box.add(bypassBuilderBtn);
        box.add(starBuilderBtn);
        box.add(nearBuilderBtn);
        box.add(Box.createGlue());
        add(box);
    }

    /**
     * Returns the selected builder
     */
    public ConnectionBuilder getSelectedBuilder() {
        if (starBuilderBtn.isSelected()) {
            return StarBuilder.create();
        } else if (nearBuilderBtn.isSelected()) {
            return ByDistanceBuilder.create();
        } else if (bypassBuilderBtn.isSelected()) {
            return BypassesBuilder.create();
        } else {
            return null;
        }
    }

    private void init() {
        ButtonGroup group = new ButtonGroup();
        group.add(starBuilderBtn);
        group.add(nearBuilderBtn);
        group.add(bypassBuilderBtn);
        bypassBuilderBtn.setSelected(true);
    }
}
