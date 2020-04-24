package beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Range {

    public Object getStart() {
        return start;
    }

    public void setStart(Object start) {
        this.start = start;
    }

    public Object getEnd() {
        return end;
    }

    public void setEnd(Object end) {
        this.end = end;
    }

    private Object start;
    private Object end;
    private int startPos;

    public int getStartPos() {
        return startPos;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    private int endPos;

    public Range() {}

    public Range(Object start, Object end) {
        this.start = start;
        this.end = end;
    }

    public String toString() {
        return String.format("Range(%s,%s)", start, end);
    }

}
