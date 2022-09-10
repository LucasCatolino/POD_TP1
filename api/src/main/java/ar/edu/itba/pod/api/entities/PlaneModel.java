package ar.edu.itba.pod.api.entities;

import java.util.Objects;

public class PlaneModel {
    int businessRows, businessCols, epRows, epCols, econRows, econCols;
    String name;

    public PlaneModel(String name, int businessRows, int businessCols, int epRows, int epCols, int econRows, int econCols) {
        this.name = name;
        this.businessRows = businessRows;
        this.businessCols = businessCols;
        this.epRows = epRows;
        this.epCols = epCols;
        this.econRows = econRows;
        this.econCols = econCols;
    }

    public int getBusinessRows() {
        return businessRows;
    }

    public int getBusinessCols() {
        return businessCols;
    }

    public int getEpRows() {
        return epRows;
    }

    public int getEpCols() {
        return epCols;
    }

    public int getEconRows() {
        return econRows;
    }

    public int getEconCols() {
        return econCols;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaneModel that = (PlaneModel) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
