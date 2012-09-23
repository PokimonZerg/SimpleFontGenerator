import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.Character.UnicodeBlock;
import javax.swing.*;
import java.util.*;
import javax.imageio.ImageIO;

public class FontMaster
{
    public FontMaster(Font font, List blocks, int x, int y, int font_size,
                      boolean aa, boolean alpha, boolean bold, boolean italic)
    { 
        symbol_data    = new ArrayList<>();
        this.font      = new Font(font.getFontName(), (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0), font_size);
        symbols        = blocksToChars(this.font, blocks);
        
        generateImage(x, y, alpha, aa);
    }
    
    public Icon getFontImage()
    {
        return new ImageIcon(image);
    }
    
    public void saveToFile(File filename)
    {
        try(BufferedWriter out = new BufferedWriter(new FileWriter(filename.getAbsoluteFile() + ".txt")))
        {
            ImageIO.write(image, "png", filename.getAbsolutePath().toLowerCase().endsWith(".png") ? 
                                        filename : new File(filename.getAbsoluteFile() +  ".png"));
            
            for(SymbolData sd: symbol_data)
            {
                out.write(sd.toString());
                out.newLine();
            }
        } 
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(null, "Беда! Звони в колокола!");
        }
    }
    
    private void generateImage(int x, int y, boolean alpha, boolean anti_aliansing)
    {
        ArrayList<GlyphVector> gv     = new ArrayList<>();
        FontRenderContext      frc    = new FontRenderContext(new AffineTransform(), anti_aliansing, true);
        char[]                 one    = new char[1];
        float                  p_x    = x / 100.0f;
        float                  p_y    = y / 100.0f;
        float                  width  = 0;
        float                  height = 0;
        float                  max_y  = 0;
        Graphics2D             g;
        
        image = new BufferedImage(x, y, alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        g     = (Graphics2D)image.getGraphics();
        
        // Generate glyph vectors
        for(int i = 0; i < symbols.length; i++)
        {
            one[0] = symbols[i];
            gv.add(font.createGlyphVector(frc, one));
        }
        
        // Get max height of first line
        for(int i = 0, w = 0; i < gv.size(); i++)
        {
            if(gv.get(i).getLogicalBounds().getHeight() > height)
            {
                height = (float)gv.get(i).getLogicalBounds().getHeight();
            }
            
            if ((w += gv.get(i).getGlyphPixelBounds(0, frc, 0, 0).width) > x)
            {
                break;
            }
        }
        
        // Draw characters
        for(int i = 0; i < gv.size(); i++)
        {  
            GlyphVector v     = gv.get(i);
            Rectangle2D r     = v.getLogicalBounds();
            
            float       vec_x = (float)r.getWidth();
            float       vec_y = (float)r.getHeight();
            
            max_y = vec_y > max_y ? vec_y : max_y;
            
            if(width + vec_x > x)
            {
                height += max_y + 1.0f;
                width = 0;
                max_y = 0;
            }
            
            // generate texture coords
            float tex_coords[] = {1.0f - ((width / p_x)           / 100.0f), (height / p_y)           / 100.0f,
                                  1.0f - ((width / p_x)           / 100.0f), ((height - vec_y) / p_y) / 100.0f,
                                  1.0f - (((width + vec_x) / p_x) / 100.0f), ((height - vec_y) / p_y) / 100.0f,
                                  1.0f - (((width + vec_x) / p_x) / 100.0f), (height / p_y)           / 100.0f};
            
            symbol_data.add(new SymbolData(symbols[i], (int)vec_x, (int)vec_y, tex_coords));
            
            g.drawGlyphVector(v, width, height);
            width += vec_x + 1.0f;
        }
    }
    
    private char[] blocksToChars(Font font, List blocks)
    {
        Iterator<UnicodeBlock>   it         = blocks.iterator();
        ArrayList<Character>     char_array = new ArrayList<>();
        
        while(it.hasNext())
        {
            UnicodeBlock u = it.next();
        
            for(char i = 0; i < Character.MAX_VALUE; i++)
            {
                if(u.equals(UnicodeBlock.of(i)) && font.canDisplay(i))
                {
                        char_array.add(i);
                }
            }
        }
        
        char char_native_array[] = new char[char_array.size()];
        
        for(int i = 0; i < char_array.size(); i++)
        {
            char_native_array[i] = char_array.get(i);
        }
        
        return char_native_array;
    }
    
    private Font                  font;
    private char                  symbols[];
    private BufferedImage         image;
    private ArrayList<SymbolData> symbol_data;
}

class SymbolData
{
    public SymbolData(char uni_id, int size_x, int size_y, 
                      float tex_coords[])
    {
        tex_coord    = tex_coords;
        symbol       = uni_id;
        x_size       = size_x;
        y_size       = size_y;
    }
    
    @Override
    public String toString()
    {
        return String.format("%d %d %d %s", (int)symbol, x_size, y_size, Arrays.toString(tex_coord));
    }
    
    private char  symbol;
    private int   x_size;
    private int   y_size;
    private float tex_coord[];
}