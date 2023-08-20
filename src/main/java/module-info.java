module org.unibl.etf {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.slf4j;
    requires javafaker;
    requires org.apache.commons.io;

    opens org.unibl.etf to javafx.fxml;
    exports org.unibl.etf;
    exports org.unibl.etf.controller;
    opens org.unibl.etf.controller to javafx.fxml;

    opens org.unibl.etf.gui to javafx.fxml;
    exports org.unibl.etf.gui;

    exports org.unibl.etf.util;
    opens org.unibl.etf.util to javafx.base;

    opens org.unibl.etf.domain to javafx.base;
    exports org.unibl.etf.domain;
    exports org.unibl.etf.domain.vehicle;
    opens org.unibl.etf.domain.vehicle to javafx.base;
    exports org.unibl.etf.domain.passenger;
    opens org.unibl.etf.domain.passenger to javafx.base;
    exports org.unibl.etf.domain.terminal;
    opens org.unibl.etf.domain.terminal to javafx.base;

}
