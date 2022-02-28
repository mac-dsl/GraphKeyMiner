package Infra;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class SummaryVertex extends Vertex implements Serializable {


    private int id;
    private int count=0;


    public SummaryVertex(int id, String type) {
        super(type.toLowerCase());
        this.id=id;
    }

    @Override
    public String toString() {
        return "vertex{" +
                "type='" + getTypes() + '\'' +
                ", attributes=" + super.getAllAttributesList() +
                '}';
    }

    public int getCount() {
        return count;
    }

    public int getId() {
        return id;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void incrementCount()
    {
        this.count++;
    }

    @Override
    public boolean isMapped(Vertex o) {

        if(o instanceof SummaryVertex)
        {
            SummaryVertex v=(SummaryVertex) o;
            return (this.getTypes().contains(v.getTypes().iterator().next()));
        }
        else
            return false;
    }

    @Override
    public int compareTo(@NotNull Vertex o) {
        if(o instanceof SummaryVertex)
        {
            SummaryVertex v=(SummaryVertex) o;
            return (this.getTypes().contains(v.getTypes().iterator().next())) ? 1 : 0;
        }
        else
            return 0;
    }
}
