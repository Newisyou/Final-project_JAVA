import java.io.FileReader;
import java.io.IOException;
import au.com.bytecode.opencsv.CSVReader;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        var teams = new ArrayList<Team>();
        var connection = DriverManager.getConnection("jdbc:sqlite:teams.db");
        var stm = connection.createStatement();

        stm.execute("drop table 'teams';");
        stm.execute("CREATE TABLE 'teams' ('name' text, 'team' text, 'position' text,'height' int, 'weight' int, 'age' real);");
        stm.close();

        var s = connection.prepareStatement("INSERT INTO 'teams' ('name','team','position','height','weight','age') VALUES (?,?,?,?,?,?);");

        try (CSVReader reader = new CSVReader(new FileReader("Показатели спортивных команд.csv"))) {
            String[] lineInArray;
            reader.readNext();
            while ((lineInArray = reader.readNext()) != null) {

                var name = lineInArray[0];
                var team = lineInArray[1];
                var position = lineInArray[2];
                var height = Integer.parseInt(lineInArray[3]);
                var weight = Integer.parseInt(lineInArray[4]);
                var age = Double.parseDouble(lineInArray[5]);

                s.setString(1, name);
                s.setString(2, team);
                s.setString(3, position);
                s.setInt(4, height);
                s.setInt(5, weight);
                s.setDouble(6, age);
                s.executeUpdate();

                teams.add(new Team(name, team, position, height, weight, age));
            }
        }

        s.close();

        System.out.println("Постройте график по среднему возрасту во всех командах. Файл excel");
        var query1 = "select team, avg(height) as h from teams group BY team;";
        stm = connection.createStatement();
        var result1 = stm.executeQuery(query1);

        while (result1.next()) {
            System.out.println(result1.getString("team").strip()+"="+result1.getString("h").replace('.',','));
        }

        //2
        System.out.println("Найдите команду с самым высоким средним ростом. Выведите в консоль 5 самых высоких игроков команды.");

        var query2 = "select team, avg(height) as h from teams group BY team;";
        var result2 = stm.executeQuery(query2);
        var team2 = "";
        double maxHeight = -1;


        while (result2.next()) {
            var height = result2.getDouble("h");
            if (height > maxHeight) {
                maxHeight = height;
                team2 = result2.getString("team");
            }
        }

        result2 = stm.executeQuery("select * from teams WHERE team LIKE '%" + team2 + "%' ORDER BY  height DESC LIMIT 5;");

        while (result2.next()) {
            System.out.println(result2.getString("name")+ " "+result2.getString("height"));
        }

        //3
        var query3 = "select team,a from (select * from (select *,team, avg(height) as h,avg(weight) as w,avg(age) as a from teams group BY team) where (h BETWEEN 74 and 78) and (w BETWEEN 190 and 210));";
        var result3 = stm.executeQuery(query3);

        var team3 = "";
        double maxAge = -1;

        while (result3.next()) {
            var age = result3.getDouble("a");
            if (age > maxAge) {
                maxAge = age;
                team3 = result3.getString("team");
            }
        }
        stm.close();
        System.out.println("Найдите команду, с средним ростом равным от 74 до 78 inches и средним весом от 190 до 210 lbs, с самым высоким средним возрастом.");
        System.out.println(team3);
    }
}
