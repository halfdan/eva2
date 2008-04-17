package eva2.gui;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 28.08.2003
 * Time: 11:10:28
 * To change this template use Options | File Templates.
 */
public class PropertyFilePath implements java.io.Serializable {
    
    public String FileName = "";
    public String FilePath = "";
    public String FileExtension = "";

    public PropertyFilePath(String s) {
        this.setCompleteFilePath(s);
    }

    public PropertyFilePath(PropertyFilePath d) {
        this.FileName       = d.FileName;
        this.FilePath       = d.FilePath;
        this.FileExtension  = d.FileExtension;
    }

    public Object clone() {
        return (Object) new PropertyFilePath(this);
    }

    /** This method will allow you to set a complete string
     * which will be separated into Path, Name and extension
     * @param s     The complete filepath and filename
     */
    public void setCompleteFilePath(String s) {
        boolean     trace = false;
        String      filesep;

        String old = this.getCompleteFilePath();
        try {
            if (trace) System.out.println("Complete Filename: " +s);
            filesep         = System.getProperty("file.separator");
            if (trace) System.out.println("File.Separator: " +filesep);
            this.FileName   = s.substring(s.lastIndexOf(filesep)+1);
            this.FileExtension = this.FileName.substring(this.FileName.lastIndexOf("."));
            this.FilePath   = s.substring(0, s.lastIndexOf(filesep)+1);

            if (trace) System.out.println("FilePath: " +this.FilePath);
            if (trace) System.out.println("Filename: " + this.FileName);
            if (trace) System.out.println("Fileext.: " + this.FileExtension);
        } catch (Exception e) {
            this.setCompleteFilePath(old);
        }
    }

    /** This method will return the complete name of the file
     * which filepath
     * @return The complete filename with path.
     */
    public String getCompleteFilePath() {
        return this.FilePath + this.FileName;
    }
}
