package edu.jiangxin.apktoolbox.i18n;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import edu.jiangxin.apktoolbox.swing.extend.JEasyPanel;
import edu.jiangxin.apktoolbox.utils.Constants;
import edu.jiangxin.apktoolbox.utils.Utils;

/**
 * @author jiangxin
 * @author 2019-04-12
 *
 */
public class I18nRemovePanel extends JEasyPanel {
    private static final long serialVersionUID = 1L;

    List<I18NInfo> infos = new ArrayList<I18NInfo>();

    private static final String charset = "UTF-8";
    
    private static final int PANEL_WIDTH = Constants.DEFAULT_WIDTH - 50;

    private static final int PANEL_HIGHT = 110;
    
    private static final int CHILD_PANEL_HIGHT = 30;
    
    private static final int CHILD_PANEL_LEFT_WIDTH = 600;
    
    private static final int CHILD_PANEL_RIGHT_WIDTH = 130;

    private JPanel sourcePanel;

    private JTextField srcTextField;

    private JButton srcButton;

    private JPanel itemPanel;

    private JTextField itemTextField;

    private JLabel itemLabel;

    private JPanel operationPanel;

    private JButton removeButton;

    public I18nRemovePanel() throws HeadlessException {
        super();
        Utils.setJComponentSize(this, PANEL_WIDTH, PANEL_HIGHT);

        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(boxLayout);

        createSourcePanel();
        add(sourcePanel);
        
        add(Box.createVerticalStrut(Constants.DEFAULT_Y_BORDER));

        createItemPanel();
        add(itemPanel);
        
        add(Box.createVerticalStrut(Constants.DEFAULT_Y_BORDER));

        createOperationPanel();
        add(operationPanel);
    }

    private void createOperationPanel() {
        operationPanel = new JPanel();
        Utils.setJComponentSize(operationPanel, PANEL_WIDTH, CHILD_PANEL_HIGHT);
        operationPanel.setLayout(new BoxLayout(operationPanel, BoxLayout.X_AXIS));
        
        removeButton = new JButton(bundle.getString("i18n.remove.title"));
        Utils.setJComponentSize(removeButton, CHILD_PANEL_RIGHT_WIDTH, CHILD_PANEL_HIGHT);
        removeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                infos.clear();
                File srcFile = new File(srcTextField.getText());
                if (!srcFile.exists() || !srcFile.isDirectory()) {
                    logger.error("srcFile is invalid");
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(I18nRemovePanel.this, "Source directory is invalid", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                    srcTextField.requestFocus();
                    return;
                }
                String srcPath;
                try {
                    srcPath = srcFile.getCanonicalPath();
                } catch (IOException e2) {
                    logger.error("getCanonicalPath fail");
                    return;
                }
                conf.setProperty("i18n.remove.src.dir", srcPath);

                String item = itemTextField.getText();
                if (StringUtils.isEmpty(item)) {
                    logger.error("item is empty");
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(I18nRemovePanel.this, "item is empty", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                    itemTextField.requestFocus();
                    return;
                }

                conf.setProperty("i18n.remove.items", item);
                remove(srcPath, itemTextField.getText());
            }
        });

        operationPanel.add(removeButton);
    }

    private void createItemPanel() {
        itemPanel = new JPanel();
        Utils.setJComponentSize(itemPanel, PANEL_WIDTH, CHILD_PANEL_HIGHT);
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.X_AXIS));
        
        itemTextField = new JTextField();
        Utils.setJComponentSize(itemTextField, CHILD_PANEL_LEFT_WIDTH, CHILD_PANEL_HIGHT);
        itemTextField.setText(conf.getString("i18n.remove.items"));

        itemLabel = new JLabel("Items");
        Utils.setJComponentSize(itemLabel, CHILD_PANEL_RIGHT_WIDTH, CHILD_PANEL_HIGHT);

        itemPanel.add(itemTextField);
        itemPanel.add(Box.createHorizontalGlue());
        itemPanel.add(itemLabel);
    }

    private void createSourcePanel() {
        sourcePanel = new JPanel();
        Utils.setJComponentSize(sourcePanel, PANEL_WIDTH, CHILD_PANEL_HIGHT);
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.X_AXIS));
        
        srcTextField = new JTextField();
        Utils.setJComponentSize(srcTextField, CHILD_PANEL_LEFT_WIDTH, CHILD_PANEL_HIGHT);
        srcTextField.setText(conf.getString("i18n.remove.src.dir"));

        srcButton = new JButton("Source Directory");
        Utils.setJComponentSize(srcButton, CHILD_PANEL_RIGHT_WIDTH, CHILD_PANEL_HIGHT);
        srcButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jfc.setDialogTitle("select a directory");
                int ret = jfc.showDialog(new JLabel(), null);
                switch (ret) {
                case JFileChooser.APPROVE_OPTION:
                    File file = jfc.getSelectedFile();
                    srcTextField.setText(file.getAbsolutePath());
                    break;
                default:
                    break;
                }

            }
        });

        sourcePanel.add(srcTextField);
        sourcePanel.add(Box.createHorizontalGlue());
        sourcePanel.add(srcButton);
    }

    private void remove(String sourceBaseStr, String itemName) {
        File[] sourceParentFiles = new File(sourceBaseStr).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith("values");
            }
        });
        if (sourceParentFiles == null) {
            logger.error("None valid directory found");
            return;
        }
        int count = 0;
        for (File sourceParentFile : sourceParentFiles) {
            File sourceFile = new File(sourceParentFile, "strings.xml");
            if (sourceFile.exists()) {
                try {
                    System.out.println("read from: " + sourceFile.getCanonicalPath());
                    String content = FileUtils.readFileToString(sourceFile, charset);
                    Pattern pattern = Pattern.compile("\\s*<string name=\"" + itemName + "\".*>.*</string>");
                    Matcher matcher = pattern.matcher(content);
                    String resultString = matcher.replaceAll("");
                    FileUtils.writeStringToFile(sourceFile, resultString, charset);
                    logger.info("remove success, count: " + (++count) + ", and file: " + sourceFile);
                } catch (IOException e) {
                    logger.error("remove exception: " + sourceFile, e);
                    continue;
                }
            }
        }
    }
}
