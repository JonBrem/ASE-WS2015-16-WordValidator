package de.ur.ahci.tuples;

import java.util.Objects;

public class Tuple {

    private String string;

    public Tuple(String s) {
        this.string = s;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public boolean equals(Object another) {
        if(another instanceof Tuple) {
            return string.equals(((Tuple) another).getString());
        } else {
            return false;
        }
    }

}
