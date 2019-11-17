package View;

import Controller.Controller;


public class MyView implements IView {

    private Controller controller;

    public MyView(Controller controller) {
        this.controller = controller;
    }

}