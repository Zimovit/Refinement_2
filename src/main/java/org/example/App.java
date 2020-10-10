package org.example;

import javax.swing.*;
import javax.swing.plaf.basic.BasicBorders;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigInteger;

public class App extends JPanel implements MouseListener, ActionListener
{
    private static final int MATERIAL_COST = 25000;
    private static final int IMPROVED_MATERIAL_COST = 125000;
    private static final int REFINEMENT_COST = 10000;
    private static int itemPrice;
    private static double chanceToRefine = 0.5, chanceToBrake = 0.25;
    private static int numberOfTries = 1000000;
    private static int[][] safeRefinementTable ={{1, 5, 100000}, {2, 10, 220000}, {3, 15, 470000}, {4, 25, 910000}, {6, 50, 1630000}, {10, 85, 2740000}};
    private JTextField price = new JTextField("0"), numberOfTriesField = new JTextField(String.valueOf(this.numberOfTries));
    private JLabel priceLabel = new JLabel("Цена шмотки:"), numberOfTriesLabel = new JLabel("Количество попыток:");
    private JButton calculateButton = new JButton("Рассчитать!");
    private JLabel[][]labels;


    public App (){

        //начнём с верхней панели

        JPanel upperPane = new JPanel();
        upperPane.setLayout(new FlowLayout(0, 5, 5));
        upperPane.add(priceLabel);
        upperPane.add(price);
        price.setPreferredSize(new Dimension(80, 20));
        upperPane.add(numberOfTriesLabel);
        upperPane.add(numberOfTriesField);
        numberOfTriesField.setPreferredSize(new Dimension(100, 20));
        upperPane.add(calculateButton);
        calculateButton.addActionListener(this);
        price.addActionListener(this);
        numberOfTriesField.addActionListener(this);

        //теперь пилим табличку
        labels = new JLabel[7][4]; //6 строк, 4 столбца в таблице
        //Инициализация столбцов
        for (int i = 5; i <= 10; i++){
            labels[i-5][0] = new JLabel("Заточка на +10 с безопасной заточкой на +" + i);
            labels[i-5][2] = new JLabel("На +15");
            labels[i-5][1] = new JLabel("0");
            labels[i-5][3] = new JLabel("0");
        }
        labels[6][0] = new JLabel("Полностью небезопасная");
        labels[6][2] = new JLabel("На +15");
        labels[6][1] = new JLabel("0");
        labels[6][3] = new JLabel("0");

        //Добавляем элементы в табличку
        JPanel table = new JPanel();
        table.setLayout(new GridLayout(7, 4, 5, 5));
        for (int row = 0; row < 7; row++){
            for (int column = 0; column < 4; column++){
                JLabel label = labels[row][column];
                label.setBorder(BorderFactory.createLineBorder(Color.GRAY,1));
                table.add(label);
            }
        }

        //корневой узел
        setLayout(new BorderLayout(5, 5));
        add(upperPane, BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);


    }


    private static int unsafeRefinement(int startRefinement, int finalRefinement) throws IllegalArgumentException {
        //нужно проверить, есть ли смысл точиться
        if (startRefinement == finalRefinement) return 0;   //уже заточено
        if ((startRefinement > finalRefinement)) throw new IllegalArgumentException("Начальный уровень выше целевого");

        //и вообще верные ли параметры

        if (startRefinement < 4 || finalRefinement > 10) throw new IllegalArgumentException("Неверно указаны уровни заточки");

        int cost = 0;   //эта переменная будет хранить стоимость заточки от начальной до желаемой.

        boolean isBroken = false;  //отдаём в заточку целую вещь

        //refLevel - на этот уровень пытаемся точиться
        for (int refLevel = startRefinement+1; refLevel <= finalRefinement;){
            //платим за материалы и за заточку
            cost = cost + MATERIAL_COST + (REFINEMENT_COST * refLevel);

            if (isBroken) {
                cost = cost + itemPrice;  //чинимся
                isBroken = false;   //и починились
            }

            boolean success = Math.random() < chanceToRefine;  //пан или пропал
            if (success){
                //точнулись, оплата уже прошла, просто повышаем уровень и на норвый цикл
                refLevel++;
            } else {
                //а тут всё фигово. Для начала слетает заточка на 1
                if (refLevel > 4) refLevel--;

                //а ведь можно ещё и сломаться
                if (Math.random() < chanceToBrake) isBroken = true;  //хрусть

            }
        }

        return cost;

    }

    private static int safeRefinement(int goalLevel) throws IllegalArgumentException {
        //проверим корректность аргумента
        if (goalLevel < 5 || goalLevel > 10) throw new IllegalArgumentException("Неверно указан уровень заточки");

        int cost = 0;   //здесь собираем стоимость
        int iterNumber = goalLevel-5; //побежим по табличке, заточка на +5 - нулевая строка таблицы
        for (int i = 0; i <= iterNumber; i++){
            cost = cost + itemPrice*safeRefinementTable[i][0] + MATERIAL_COST*safeRefinementTable[i][1] + safeRefinementTable[i][2];
        }
        return cost;
    }

    private static int improvedRefinement(){
        int cost = 0;

        for (int i = 0; i < 5;){
            cost += 225000;
            if (Math.random() < chanceToRefine){
                i++;
            } else {
                i--;
                if (i < 0) {
                    cost = cost + unsafeRefinement(9, 10);
                    i = 0;
                }
                cost += itemPrice;
            }
        }
        return cost;
    }

    private void calculate() {
        int tries;

        try{
            itemPrice = Integer.parseInt(price.getText().trim());
            tries = Integer.parseInt(numberOfTriesField.getText().trim());
        }
        catch (NumberFormatException e){
            showError();
            return;
        }

        if (itemPrice <= 0 || tries <= 0) {
            showError();
            return;
        }
        //точка до +4
        int refinementPrice = itemPrice + MATERIAL_COST*4 + REFINEMENT_COST*10;

        for (int i = 0; i < 6; i++){
            BigInteger unsafeMediumCost = new BigInteger("0");
            for (int j = 0; j < tries; j++){
                unsafeMediumCost = unsafeMediumCost.add(new BigInteger(String.valueOf(unsafeRefinement(i+5, 10))));

            }
            unsafeMediumCost = unsafeMediumCost.divide(new BigInteger(String.valueOf(tries)));
            int cost = refinementPrice + safeRefinement(i+5) + unsafeMediumCost.intValue();
            labels[i][1].setText(String.valueOf(cost));
            unsafeMediumCost = new BigInteger("0");
            for (int j = 0; j < tries; j++){
                unsafeMediumCost = unsafeMediumCost.add(new BigInteger(String.valueOf(improvedRefinement())));
            }
            unsafeMediumCost = unsafeMediumCost.divide(new BigInteger(String.valueOf(tries)));
            cost = cost+unsafeMediumCost.intValue();
            labels[i][3].setText(String.valueOf(cost));
        }

        BigInteger unsafeMediumCost = new BigInteger("0");
        for (int j = 0; j < tries; j++){
            unsafeMediumCost = unsafeMediumCost.add(new BigInteger(String.valueOf(unsafeRefinement(4, 10))));

        }
        unsafeMediumCost = unsafeMediumCost.divide(new BigInteger(String.valueOf(tries)));
        int cost = refinementPrice + unsafeMediumCost.intValue();
        labels[6][1].setText(String.valueOf(cost));

        unsafeMediumCost = new BigInteger("0");
        for (int j = 0; j < tries; j++){
            unsafeMediumCost = unsafeMediumCost.add(new BigInteger(String.valueOf(improvedRefinement())));
        }
        unsafeMediumCost = unsafeMediumCost.divide(new BigInteger(String.valueOf(tries)));

        labels[6][3].setText(String.valueOf(cost+unsafeMediumCost.intValue()));
    }

    //показываем сообщение об ошибке
    private void showError(){
        JOptionPane.showMessageDialog(this, "Нужно ввести целые положительные числа, а не какую-то фигню.");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        calculate();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        calculate();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
