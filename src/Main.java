import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static DataSource dataSource = createDataSource();


    public static void main(String[] args) {

        try (
                Connection connection = dataSource.getConnection();
                Statement stmt = connection.createStatement();
        ) {

            int izbor;

            do {
                ispisiIzbornik();
                System.out.printf("Odaberite broj radnje koju zelite izvrsiti: ");
                izbor = Integer.parseInt(sc.nextLine());

                switch (izbor) {
                    case 1:
                        dodajNovogPolaznika(connection, stmt);
                        break;
                    case 2:
                        dodajNoviProgramObrazovanja(connection, stmt);
                        break;
                    case 3:
                        upisiPolaznikaNaProgramObrazovanja(connection, stmt);
                        break;
                    case 4:
                        prebaciPolaznikaUDrugiPO(connection, stmt);
                        break;
                    case 5:
                        pregledPolaznikaPoPO(connection, stmt);
                        break;
                    case 0:
                        System.out.println("Izlaz iz programa.");
                        break;
                    default:
                        System.out.println("Neispravan odabir.");
                }

            } while (izbor != 0);

        } catch (SQLException e) {
            System.err.println("Greska pri spajanju na bazu.");
            e.printStackTrace();
        }

    }

    // ------------------------------------------------------------------------------------------------------
    // 1. Dodaj novog polaznika
    // ------------------------------------------------------------------------------------------------------
    private static void dodajNovogPolaznika(Connection connection, Statement stmt) {
        System.out.println("\n- - - Unos novog polaznika - - -");

        System.out.printf("Ime: ");
        String ime = sc.nextLine();

        System.out.printf("Prezime: ");
        String prezime = sc.nextLine();

        Polaznik polaznik = new Polaznik(null, ime, prezime);

        try (CallableStatement cs = connection.prepareCall("{call DodajNovogPolaznika(?,?)}")) {

            connection.setAutoCommit(false);

            cs.setString(1, polaznik.getIme());
            cs.setString(2, polaznik.getPrezime());
            cs.executeUpdate();

            connection.commit();

            ResultSet resultSet = stmt.executeQuery("SELECT * FROM Polaznik WHERE Ime='" + polaznik.getIme() + "' AND Prezime='" + polaznik.getPrezime() + "'");
            System.out.println("Novi polaznik uspjesno dodan!");
            System.out.println("Ime     | Prezime");
            while (resultSet.next()) {
                System.out.printf(
                        "%s  | %s\n",
                        resultSet.getString("Ime"),
                        resultSet.getString("Prezime")
                );
            }

            resultSet.close();

        } catch (SQLException e) {
            System.err.println("Greska pri spajanju na bazu.");
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------------------------------------
    // 2. Unesi novi program obrazovanja
    // ------------------------------------------------------------------------------------------------------
    private static void dodajNoviProgramObrazovanja(Connection connection, Statement stmt) {
        System.out.println("\n- - - Unos novog programa obrazovanja - - -");

        System.out.printf("Naziv: ");
        String naziv = sc.nextLine();

        System.out.printf("CSVET: ");
        int csvet = Integer.parseInt(sc.nextLine());

        ProgramObrazovanja programObrazovanja = new ProgramObrazovanja(null, naziv, csvet);

        try (CallableStatement cs = connection.prepareCall("{call DodajNoviProgramObrazovanja(?,?)}")) {

            connection.setAutoCommit(false);

            cs.setString(1, programObrazovanja.getNaziv());
            cs.setInt(2, programObrazovanja.getCSVET());
            cs.executeUpdate();

            connection.commit();

            ResultSet resultSet = stmt.executeQuery("SELECT * FROM ProgramObrazovanja WHERE Naziv='" + programObrazovanja.getNaziv() + "' AND CSVET=" + programObrazovanja.getCSVET());
            System.out.println("Novi program obrazovanja uspjesno dodan!");
            while (resultSet.next()) {
                System.out.printf(
                        "%s  | %d\n",
                        resultSet.getString("Naziv"),
                        resultSet.getInt("CSVET")
                );
            }

            resultSet.close();

        } catch (SQLException e) {
            System.err.println("Greska pri spajanju na bazu.");
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------------------------------------
    // 3. Upiši polaznika na program obrazovanja
    // ------------------------------------------------------------------------------------------------------
    private static void upisiPolaznikaNaProgramObrazovanja(Connection connection, Statement stmt) throws SQLException {
        System.out.println("\n- - - Unos polaznika na program obrazovanja - - -");

        try (CallableStatement cs = connection.prepareCall("{call UpisiPolaznikaNaPO(?,?)}")) {
            connection.setAutoCommit(false);

            // Odabir polaznika
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM Polaznik");
            while (resultSet.next()) {
                System.out.printf(
                        "%d | %s  | %s\n",
                        resultSet.getInt("PolaznikID"),
                        resultSet.getString("Ime"),
                        resultSet.getString("Prezime")
                );
            }

            System.out.printf("Unesite ID polaznika: ");
            int idPolaznika = Integer.parseInt(sc.nextLine());

            // Odabir programa obrazovanja
            resultSet = stmt.executeQuery("SELECT * FROM ProgramObrazovanja");
            while (resultSet.next()) {
                System.out.printf(
                        "%d | %s  | %d\n",
                        resultSet.getInt("ProgramObrazovanjaID"),
                        resultSet.getString("Naziv"),
                        resultSet.getInt("CSVET")
                );
            }

            System.out.printf("Unesite ID programa obrazovanja: ");
            int idPO = Integer.parseInt(sc.nextLine());

            cs.setInt(1, idPolaznika);
            cs.setInt(2, idPO);
            cs.executeUpdate();

            connection.commit();

            resultSet = stmt.executeQuery("SELECT * FROM Upis WHERE IDProgramObrazovanja=" + idPO + " AND IDPolaznik=" + idPolaznika);
            System.out.println("Polaznik uspjesno upisan na program obrazovanja!");

            resultSet.close();

        } catch (SQLException e) {
            connection.rollback();
            System.err.println("Greska pri spajanju na bazu.");
            e.printStackTrace();
        }

    }

    // ------------------------------------------------------------------------------------------------------
    // 4. Prebaci polaznika iz jednog u drugi program obrazovanja
    // ------------------------------------------------------------------------------------------------------
    private static void prebaciPolaznikaUDrugiPO(Connection connection, Statement stmt) throws SQLException {
        System.out.println("\n- - - Prebaci polaznika iz jednog u drugi program obrazovanja - - -");

        // Odabir polaznika
        ResultSet resultSet = stmt.executeQuery("SELECT * FROM Polaznik");
        while (resultSet.next()) {
            System.out.printf(
                    "%d | %s  | %s\n",
                    resultSet.getInt("PolaznikID"),
                    resultSet.getString("Ime"),
                    resultSet.getString("Prezime")
            );
        }

        System.out.printf("Unesite ID polaznika kojeg zelite prebaciti: ");
        int idPolaznika = Integer.parseInt(sc.nextLine());

        // Ispis programa obrazovanja koja pohada odabrani polaznik
        resultSet = stmt.executeQuery(
                "SELECT po.ProgramObrazovanjaID, po.Naziv FROM ProgramObrazovanja po INNER JOIN Upis u ON po.ProgramObrazovanjaID = u.IDProgramObrazovanja WHERE u.IDPolaznik=" + idPolaznika
        );
        while (resultSet.next()) {
            System.out.printf(
                    "%d  | %s\n",
                    resultSet.getInt("ProgramObrazovanjaID"),
                    resultSet.getString("Naziv")
            );
        }

        System.out.printf("Unesite ID programa obrazovanja iz kojega zelite prebaciti polaznika s ID-em " + idPolaznika + ": ");
        int idPOPrebacitiIz = Integer.parseInt(sc.nextLine());

        // Odabir programa obrazovanja
        resultSet = stmt.executeQuery("SELECT * FROM ProgramObrazovanja");
        while (resultSet.next()) {
            System.out.printf(
                    "%d | %s  | %d\n",
                    resultSet.getInt("ProgramObrazovanjaID"),
                    resultSet.getString("Naziv"),
                    resultSet.getInt("CSVET")
            );
        }

        System.out.printf("Unesite ID programa obrazovanja u kojega zelite prebaciti polaznika s ID-em " + idPolaznika + ": ");
        int idPOPrebacitiU = Integer.parseInt(sc.nextLine());

        try (CallableStatement cs = connection.prepareCall("{call PrebaciPolaznikaUDrugiPO(?,?,?)}")) {

            cs.setInt(1, idPOPrebacitiIz);
            cs.setInt(2, idPOPrebacitiU);
            cs.setInt(3, idPolaznika);
            cs.executeUpdate();

            connection.commit();

            System.out.println("Polaznik uspjesno prebacen na drugi program obrazovanja!");

        } catch (SQLException e) {
            System.err.println("Greska pri spajanju na bazu.");
            e.printStackTrace();
        }

        resultSet.close();
    }

    // ------------------------------------------------------------------------------------------------------
    // 5. Pregled polaznika po programu obrazovanja
    // ------------------------------------------------------------------------------------------------------
    private static void pregledPolaznikaPoPO(Connection connection, Statement stmt) throws SQLException {
        System.out.println("\n- - - Pregled polaznika po programu obrazovanja - - -");

        // Odabir programa obrazovanja
        ResultSet resultSet = stmt.executeQuery("SELECT * FROM ProgramObrazovanja");
        while (resultSet.next()) {
            System.out.printf(
                    "%d | %s  | %d\n",
                    resultSet.getInt("ProgramObrazovanjaID"),
                    resultSet.getString("Naziv"),
                    resultSet.getInt("CSVET")
            );
        }

        System.out.printf("Unesite ID programa obrazovanja za kojega zelite pregledati polaznike: ");
        int idPO = Integer.parseInt(sc.nextLine());

        try (CallableStatement cs = connection.prepareCall("{call PregledPolaznikaPoPO(?)}")) {

            cs.setInt(1, idPO);
            resultSet = cs.executeQuery();

            while (resultSet.next()) {
                System.out.printf(
                        "%d | %s  | %s\n",
                        resultSet.getInt("PolaznikID"),
                        resultSet.getString("Ime"),
                        resultSet.getString("Prezime")
                );
            }

            connection.commit();

        } catch (SQLException e) {
            System.err.println("Greska pri spajanju na bazu.");
            e.printStackTrace();
        }
    }

    private static void ispisiIzbornik() {
        System.out.println("\n ———————————————————————————————————————————————————————————————");
        System.out.println("| 1. Unesi novog polaznika                                      |");
        System.out.println("| 2. Unesi novi program obrazovanja                             |");
        System.out.println("| 3. Upiši polaznika na program obrazovanja                     |");
        System.out.println("| 4. Prebaci polaznika iz jednog u drugi program obrazovanja    |");
        System.out.println("| 5. Pregled polaznika po programu obrazovanja                  |");
        System.out.println("| 0. Izlaz                                                      |");
        System.out.println(" ———————————————————————————————————————————————————————————————");
    }


    // ---------------------------------------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------------------------------------
    // DataSource
    private static DataSource createDataSource() {
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName("localhost");
        ds.setDatabaseName("JavaAdv");
        ds.setUser("sa");
        ds.setPassword("SQL");
        ds.setEncrypt(false);
        return ds;
    }

    // Helperi
    private static <T> String formatColumn(T value, long length) {
        String formattedValue = String.valueOf(value);

        if ((long) formattedValue.length() != length) {
            long spaces = length - (long) formattedValue.length();
            for (int i = 0; i < spaces; i++) {
                formattedValue += " ";
            }
        }
        return formattedValue;
    }
}