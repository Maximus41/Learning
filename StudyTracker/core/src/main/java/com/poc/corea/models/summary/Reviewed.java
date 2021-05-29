
package com.poc.corea.models.summary;

import java.util.ArrayList;
import java.util.List;

public class Reviewed {

    private List<Count> count = new ArrayList<Count>();

    public List<Count> getCount() {
        return count;
    }

    public void setCount(List<Count> count) {
        this.count = count;
    }

}
