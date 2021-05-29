
package com.poc.corea.models.session;

import java.util.ArrayList;
import java.util.List;

public class Reviewed {

    private float reviewStoryPoints = 0.125f;
    private List<Count> count = new ArrayList<Count>();

    public List<Count> getCount() {
        return count;
    }

    public void setCount(List<Count> count) {
        this.count = count;
    }

}
