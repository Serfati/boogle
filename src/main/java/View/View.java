package View;

import Controller.Controller;


public class View implements IView {

    private Controller controller;

    public View(Controller controller) {
        this.controller = controller;
    }

}