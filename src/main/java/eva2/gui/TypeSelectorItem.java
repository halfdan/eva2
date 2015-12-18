package eva2.gui;

public class TypeSelectorItem
{
    private String id;
    private String displayName;
    private String description;

    public TypeSelectorItem(String id, String displayName, String description)
    {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public String getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String toString()
    {
        return id;
    }
}
