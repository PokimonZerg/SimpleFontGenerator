import java.awt.Component;
import javax.swing.*;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.Character.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileFilter;

public class Window
{
    public Window()
    {
        file_chooser = new JFileChooser();
        fonts        = InitFonts();
        blocks       = InitUnicodeBlocks();
        frame        = InitWindow();
        
        file_chooser.setFileFilter(new PngFilter());
    }
    
    public void Show()
    {
        frame.setVisible(true);
    }
    
    private ArrayList<Font> InitFonts()
    {
        enviroment                  = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font temp_fonts[]           = enviroment.getAllFonts();
        ArrayList<Font> fonts_array = new ArrayList<>();
        
        for(int i = 0; i < temp_fonts.length; i++)
        {
            if(temp_fonts[i].canDisplayUpTo(temp_fonts[i].getFontName()) == -1)
            {
                fonts_array.add(temp_fonts[i].deriveFont(Font.PLAIN, 16.0f));
            }
        }
        
        return fonts_array;
    }
    
    private UnicodeBlock[] InitUnicodeBlocks()
    {
        Field        fields[];
        UnicodeBlock ub[];
        
        try {
            fields  = UnicodeBlock.class.getFields();
            ub      = new UnicodeBlock[fields.length];
            
            for(int i = 0; i < fields.length; i++) {
                ub[i] = (UnicodeBlock)fields[i].get(new Object());
            }
        }
        catch(SecurityException | IllegalArgumentException | IllegalAccessException e) {
            ub = null;
        }

        return ub;
    }
    
    private JFrame InitWindow()
    {
        frame        = new JFrame("Simple Font Generator");
        font_box     = new JComboBox(fonts.toArray());
        main_panel   = new JPanel();
        pic_label    = new JLabel();
        code_list    = new JList(blocks);
        scroll_pane  = new JScrollPane(pic_label);
        list_pane    = new JScrollPane(code_list);
        
        frame.setBounds(256, 256, 768, 498);
        frame.setLayout(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(main_panel);
        frame.setVisible(true);
        main_panel.setLayout(null);
        addComponents(main_panel, font_box, scroll_pane, list_pane,
                      gen_button   = newButton("Generate", 8, 432, 124, 28),
                      save_button  = newButton("Save", 140, 432, 124, 28),
                      size_x_field = newTextField("512", 8, 304, 48, 24),
                      size_y_field = newTextField("512", 80, 304, 48, 24),
                      font_field   = newTextField("32", 8, 336, 48, 24), 
                      size_label   = newLabel("Image size", 136, 304, 64, 24), 
                      font_label   = newLabel("Font size", 136, 336, 64, 24), 
                      x_label      = newLabel("X", 64, 312, 8, 8),
                      aa_box       = newCheckBox("Anti-aliasing", 8, 368, 128, 24),
                      alpha_box    = newCheckBox("Alpha texture", 144, 368, 128, 24),
                      bold_box     = newCheckBox("Bold", 8, 400, 128, 24),
                      italic_box   = newCheckBox("Italic", 144, 400, 128, 24));

        list_pane.setBounds(8, 40, 256, 256);
        list_pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll_pane.setBounds(272, 8, 485, 452);
        font_box.setBounds(8, 8, 256, 24);
        font_box.setRenderer(new FontBoxRenderer());
        font_box.setMaximumRowCount(20);
        font_box.updateUI();
        gen_button.addActionListener(new GenListener());
        save_button.addActionListener(new SaveListener());
        scroll_pane.revalidate();
        
        return frame;
    }
    
    private void addComponents(JPanel panel, JComponent ...controls)
    {
        for(JComponent c: controls)
        {
            panel.add(c);
        }
    }
    
    private JButton newButton(String name, int x, int y, int width, int height)
    {
        JButton button = new JButton(name);
        
        button.setBounds(x, y, width, height);
        button.setFocusable(false);
        
        return button;
    }
    
    private JLabel newLabel(String name, int x, int y, int width, int height)
    {
        JLabel label = new JLabel(name);
        
        label.setBounds(x, y, width, height);
        
        return label;
    }
    
    private JCheckBox newCheckBox(String name, int x, int y, int width, int height)
    {
        JCheckBox box = new JCheckBox(name);
        
        box.setBounds(x, y, width, height);
        
        return box;
    }
    
    private JTextField newTextField(String name, int x, int y, int width, int height)
    {
        JTextField field = new JTextField(name);
        
        field.setBounds(x, y, width, height);
        
        return field;
    }
    
    private FontMaster          font_master ;
    private Icon                icon        ;
    private ArrayList<Font>     fonts       ;
    private UnicodeBlock[]      blocks      ;
    private GraphicsEnvironment enviroment  ;
    private JFrame              frame       ;
    private JList               code_list   ;
    private JPanel              main_panel  ;
    private JComboBox           font_box    ;
    private JFileChooser        file_chooser;
    private JScrollPane         scroll_pane , list_pane   ;
    private JButton             gen_button  , save_button ;
    private JTextField          size_x_field, size_y_field, font_field;
    private JCheckBox           aa_box      , alpha_box   , bold_box  , italic_box;
    private JLabel              pic_label   , size_label  , font_label, x_label   ;
    
    class GenListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            boolean anti_aliansing = aa_box.isSelected();
            boolean alpha          = alpha_box.isSelected();
            boolean bold           = bold_box.isSelected();
            boolean italic         = italic_box.isSelected();
            Font    font           = (Font)font_box.getSelectedItem();
            List    ub_list        = code_list.getSelectedValuesList();
            int     font_size      = Integer.parseInt(font_field.getText());
            int     size_x         = Integer.parseInt(size_x_field.getText());
            int     size_y         = Integer.parseInt(size_y_field.getText());
            
            font_master            = new FontMaster(font, ub_list, size_x,
                                         size_y, font_size,anti_aliansing,
                                         alpha, bold, italic);
            
            pic_label.setIcon(icon = font_master.getFontImage());
        }
    }
    
    class SaveListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if(file_chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
            { 
                if(font_master == null)
                {
                    JOptionPane.showMessageDialog(frame,
                            "Сначала картинку сгенерируй а потом сохраняй! \r\n"    + 
                            "Еще раз тыкнешь бес спроса - сломаю тут что нибудь!!!");
                }
                else
                {
                    font_master.saveToFile(file_chooser.getSelectedFile());
                }
            }
        }
    }
}

class FontBoxRenderer extends JLabel implements ListCellRenderer
{
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        Font f = (Font)value;
        super.setText(f.getFontName());
        super.setFont(f);

        return this;
    }
}

class PngFilter extends FileFilter
{
    @Override
    public boolean accept(File f)
    {
        return f.getName().toLowerCase().endsWith(".png");
    }

    @Override
    public String getDescription()
    {
        return "PNG";
    }
}