package com.flow.forum.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Page {

    // current page
    private int current = 1;

    //number of posts per page
    private int limit = 10;

    //total number of posts, rows of data
    private int rows;

    //query path,  reusable pagination links
    private String path;


    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public int getOffset() {
        // current * limit - limit
        return (current - 1) * limit;
    }

    //get total pages
    public int getTotal() {
        // rows / limit
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }

    //starting page number for pagination display
    public int getStartNum() {
        int startNum = current - 2;
        return Math.max(startNum, 1);
    }

    //ending page number for pagination display
    public int getEndNum() {
        int endNum = current + 2;
        int total = getTotal();
        return Math.min(endNum, total);
    }
}
