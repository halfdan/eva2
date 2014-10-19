package eva2.gui;

import eva2.tools.ReflectPackage;

/**
 *
 */
public class PropertyFilePath implements java.io.Serializable {

    public String fileName = "";
    public String filePath = "";
    public String fileExtension = "";

    /**
     * Constructor setting the absolute path. F
     *
     * @param s
     */
    private PropertyFilePath(String s) {
        this.setCompleteFilePath(s);
    }

    public PropertyFilePath(PropertyFilePath d) {
        this.fileName = d.fileName;
        this.filePath = d.filePath;
        this.fileExtension = d.fileExtension;
    }

    /**
     * Get an instance by an absolute path.
     *
     * @param path
     * @return
     */
    public static PropertyFilePath getFilePathAbsolute(String path) {
        return new PropertyFilePath(path);
    }

    /**
     * Get an instance by a relative path.
     *
     * @param relPath
     * @return
     */
    public static PropertyFilePath getFilePathFromResource(String relPath) {
        String fName = ReflectPackage.getResourcePathFromCP(relPath);
        if (fName == null) {
            return null;
        } else {
            return new PropertyFilePath(fName);
        }
    }

    @Override
    public Object clone() {
        return new PropertyFilePath(this);
    }

    /**
     * This method will allow you to set a complete string
     * which will be separated into Path, Name and extension
     *
     * @param s The complete filepath and filename
     */
    public void setCompleteFilePath(String s) {
        boolean trace = false;
        String filesep;

        String old = this.getCompleteFilePath();
        try {
            if (trace) {
                System.out.println("Complete Filename: " + s);
            }
            filesep = System.getProperty("file.separator");
            if (trace) {
                System.out.println("File.Separator: " + filesep);
            }
            this.fileName = s.substring(s.lastIndexOf(filesep) + 1);
            this.fileExtension = this.fileName.substring(this.fileName.lastIndexOf("."));
            this.filePath = s.substring(0, s.lastIndexOf(filesep) + 1);

            if (trace) {
                System.out.println("filePath: " + this.filePath);
            }
            if (trace) {
                System.out.println("Filename: " + this.fileName);
            }
            if (trace) {
                System.out.println("Fileext.: " + this.fileExtension);
            }
        } catch (Exception e) {
            this.setCompleteFilePath(old);
        }
    }

    /**
     * This method will return the complete name of the file
     * which filepath
     *
     * @return The complete filename with path.
     */
    public String getCompleteFilePath() {
        return this.filePath + this.fileName;
    }
}
