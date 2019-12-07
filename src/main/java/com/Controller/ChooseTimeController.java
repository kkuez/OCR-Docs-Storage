package com.Controller;

import com.Controller.Reporter.ChooseTimeReporter;
import com.Controller.Reporter.Reporter;
import com.Utils.TimeUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChooseTimeController extends Controller{

    @FXML
    private Button submitButton;

    @FXML
    private TextField specificTimeOneTextField;

    @FXML
    private TextField specificTimeTwoTextField;

    @FXML
    private RadioButton lastMonthRadio;

    @FXML
    private RadioButton lastSixMonthRadio;

    @FXML
    private RadioButton lastYearRadio;

    @FXML
    private RadioButton specificTimeRadio;

    private ChooseTimeReporter reporter;

    private List<RadioButton> radioButtons;


    public void initialize(){
        radioButtons = new ArrayList<>(4);
        radioButtons.add(lastMonthRadio);
        radioButtons.add(lastSixMonthRadio);
        radioButtons.add(lastYearRadio);
        radioButtons.add(specificTimeRadio);
    }

    public void checklastMonthRadio(){
        radioButtons.forEach(radioButton -> radioButton.setSelected(false));
        lastMonthRadio.setSelected(true);
    }

    public void checklastSixMonthRadio(){
        radioButtons.forEach(radioButton -> radioButton.setSelected(false));
        lastSixMonthRadio.setSelected(true);
    }
    public void checklastYearRadio(){
        radioButtons.forEach(radioButton -> radioButton.setSelected(false));
        lastYearRadio.setSelected(true);}
    public void checkspecificTimeRadio(){
        radioButtons.forEach(radioButton -> radioButton.setSelected(false));
        specificTimeRadio.setSelected(true);}

    @Override
    void closeWindow() {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }

    public void setReporter(Reporter reporter){
        this.reporter = (ChooseTimeReporter) reporter;
    }

    public void submit(){
        LocalDate startDate = null;
        LocalDate endDate = null;
        if(!specificTimeRadio.isSelected()){
            if(lastMonthRadio.isSelected()){
                startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);
                endDate = LocalDate.now().minusMonths(1).withDayOfMonth(TimeUtil.getdaysOfMonthCount(LocalDate.now().minusMonths(1).getYear(), LocalDate.now().minusMonths(1).getMonth().getValue()));
            }else{
                if(lastSixMonthRadio.isSelected()){
                    startDate = LocalDate.now().minusMonths(7).withDayOfMonth(1);
                    endDate = LocalDate.now().minusMonths(1).withDayOfMonth(TimeUtil.getdaysOfMonthCount(LocalDate.now().minusMonths(1).getYear(), LocalDate.now().minusMonths(1).getMonth().getValue()));
                }else{
                    if(lastYearRadio.isSelected()){
                        startDate = LocalDate.now().minusMonths(13).withDayOfMonth(1);
                        endDate = LocalDate.now().minusMonths(1).withDayOfMonth(TimeUtil.getdaysOfMonthCount(LocalDate.now().minusMonths(1).getYear(), LocalDate.now().minusMonths(1).getMonth().getValue()));
                    }
                }
            }
        }else{
            String beginDateString = specificTimeOneTextField.getText();
            int month = Integer.parseInt(beginDateString.substring(0, beginDateString.indexOf("/")));
            int year = Integer.parseInt(beginDateString.substring(beginDateString.indexOf("/") + 1));
            startDate = LocalDate.of(year, month, 1);

            String endDateString = specificTimeTwoTextField.getText();
            int monthEnd = Integer.parseInt(endDateString.substring(0, endDateString.indexOf("/")));
            int yearEnd = Integer.parseInt(endDateString.substring(endDateString.indexOf("/") + 1));
            endDate = LocalDate.of(yearEnd, monthEnd, TimeUtil.getdaysOfMonthCount(startDate.getYear(), startDate.getMonth().getValue()));
        }

        reporter.submitTimes(startDate, endDate);
        closeWindow();
    }
}
