/*
    Group 2 edu.ucalgary.ensf409.Food Bank
    Members: Topan Budiman, Maxwell Couture, Mark Ngu, Jason Nguyen
    version: @1.6
    since: @1.0

    This class is responsible for connecting to the database as well as maintaining the
    items used in it as well
 */

package edu.ucalgary.ensf409;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class FoodBank {
    private final String DBURL;
    private final String USERNAME;
    private final String PASSWORD;
    private Connection dbConnect;
    private HashMap<Integer, Food> foodList = new HashMap<Integer, Food>();
    private ArrayList<String> foodCart = new ArrayList<String>();

    /**
     *
     * @param url is the url of the database
     * @param user is the username to access the database
     * @param pw is the password used in order to access the database
     */
    public FoodBank(String url, String user, String pw) {
        this.DBURL = url;
        this.USERNAME = user;
        this.PASSWORD = pw;
    }

    /**
     * This method is a getter for the database url
     * @return String This returns the url of the databse
     */
    public String getDburl() {
        return this.DBURL;
    }

    /**
     * This method is a getter for the username to access
     * the database
     * @return String This returns the username for the database
     */
    public String getUsername() {return this.USERNAME;}

    /**
     * This method is a getter for the password to access
     * the database
     * @return String This returns the password for the database
     */
    public String getPassword() {return this.PASSWORD;}


    /**
     * This method is a getter that retrieves the food item from the foodList
     * hashmap, using the ID which corresponds to the key in foodList
     * @param ID This is a unique int that corresponds to a specific food object
     * @return Food This returns a Food object with the matching ID
     */
    public Food getFood(int ID){
        return this.foodList.get(ID);
    }

    /**
     * This method is responsible for searching for a Food object that has
     * macro-nutrients that are closest to the targetMacro
     * @param targetMacro This is a double that represents the macro-nutrient we
     *                    are searching for
     * @param index This is an int corresponding to each food grouping, i.e 0 is grain and 1
     *              would be fruits & veggies
     * @return int This returns an int that corresponds to the unique ID for a Food object
     */
    public int searchFood(double targetMacro, int index){
        double prevDiff = targetMacro;
        double foodMacro = 0;
        int ID = 0;
        for(Integer key : foodList.keySet()){
            Food tmpItem = foodList.get(key);
            switch(index){
                case 0:
                    foodMacro = tmpItem.getGrain();
                    break;
                case 1:
                    foodMacro = tmpItem.getFV();
                    break;
                case 2:
                    foodMacro = tmpItem.getProtein();
                    break;
                case 3:
                    foodMacro = tmpItem.getOther();
                    break;
                case 4:
                    foodMacro = tmpItem.getCalories();
                    break;
            }
            double currDiff = Math.abs(targetMacro - foodMacro);
            if(currDiff < prevDiff + 100){
                prevDiff = currDiff;
                ID = tmpItem.getID();
            }
        }
        return ID;
    }

    /**
     * This method is a recursive function that is used to fill the foodCart ArrayList
     * with Food objects that best suit the user's nutritional needs
     * @param currMacro This is a double that represents the value of the
     *                  macro-nutrient that is being filled
     * @param targetMacro This is a double that represents the total value
     *                    of the specific macro-nutrient that is being filled
     * @param calculated This is a double array that contains all the calculated
     *                   value when searching for the most suitable Food items
     * @param index This is an int that corresponds to a specific food grouping
     *              i.e) 0 is for grain and 1 is for fruits & veggies
     * @return ArrayList<String> this returns an ArrayList of all the found food items
     */
    public ArrayList<String> fillFood(double currMacro, double targetMacro, double[] calculated, int index){
        if(currMacro > targetMacro){
            return foodCart;
        } else {
            int ID = searchFood(targetMacro - currMacro, index);
            Food tmpFood = getFood(ID);
            foodCart.add(tmpFood.getFoodName());
            foodList.remove(ID);
            calculated[0] += tmpFood.getGrain();
            calculated[1] += tmpFood.getFV();
            calculated[2] += tmpFood.getProtein();
            calculated[3] += tmpFood.getOther();
            calculated[4] += tmpFood.getCalories();
            fillFood(calculated[index], targetMacro, calculated, index);
        }
        return foodCart;
    }

    /**
     * This method is responsible for initializing the connection to the inventory.sql
     */
    public void initializeConnection() {
        try{
            dbConnect = DriverManager.getConnection(getDburl(), getUsername(), getPassword());
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * This method is responsible for reading the Food items from the inventory.sql
     * database and storing all the items locally. This also assigns each Food item
     * a unique ID
     */
    public void storeFood(){
        int i = 0;
        try{
            Statement myStmt = dbConnect.createStatement();
            ResultSet foodInfo = myStmt.executeQuery("SELECT * FROM AVAILABLE_FOOD");
            while(foodInfo.next()){
                Food tmp = new Food(i, foodInfo.getString("Name"), foodInfo.getInt("GrainContent"),
                        foodInfo.getInt("FVContent"), foodInfo.getInt("ProContent"),
                        foodInfo.getInt("Other"), foodInfo.getInt("Calories"));
                foodList.put(i, tmp);
                i++;
            }
            myStmt.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FoodBank cock = new FoodBank("jdbc:mysql://localhost/FOOD_INVENTORY", "root", "topanb11");
        cock.initializeConnection();
        cock.storeFood();
        double[] expected = {10332, 17136, 12772, 13230, 56700};
        double[] actual = {0, 0, 0, 0, 0};
        cock.initializeConnection();
        cock.storeFood();
        ArrayList<String> weiner = new ArrayList<>();
        for(int i = 0; i < actual.length; i++){
            weiner = cock.fillFood(actual[i], expected[i], actual, i);
        }
    }
}