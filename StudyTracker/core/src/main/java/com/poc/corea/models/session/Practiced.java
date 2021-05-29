
package com.poc.corea.models.session;

import java.util.ArrayList;
import java.util.List;

public class Practiced {

    private float practiceStoryPoints = 0.5f;

    private List<Count> count = new ArrayList<Count>();

    public List<Count> getCount() {
        return count;
    }

    public void setCount(List<Count> count) {
        this.count = count;
    }

}
