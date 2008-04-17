package eva2.server.go.tools;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 26.11.2004
 * Time: 13:34:42
 * To change this template use File | Settings | File Templates.
 */
public class GONamingBox {

    static String[] m_Names = {"Aleksei",
                        "Aleksandr",
                        "Andrei",
                        "Shurik",
                        "Andryusha",
                        "Tolyar",
                        "Anton",
                        "Arkadiy",
                        "Artur",
                        "Averiy",
                        "Borya",
                        "Boris",
                        "Dmitriy",
                        "Edik",
                        "Filipp",
                        "Fyodor",
                        "Gennadiy",
                        "Georgiy",
                        "Grigoriy",
                        "Igor",
                        "Il'ya",
                        "Innokentiy",
                        "Kirill",
                        "Ivan",
                        "Kirill",
                        "Konstantin",
                        "Leonid",
                        "Lev",
                        "Maksim",
                        "Mark",
                        "Mihail",
                        "Nikolai",
                        "Kolya",
                        "Oleg",
                        "Pavel",
                        "Pasha",
                        "Pyotr",
                        "Rodion",
                        "Rodya",
                        "Roman",
                        "Ruslan",
                        "Rustam",
                        "Semyon",
                        "Sergei",
                        "Stanislav",
                        "Stepan",
                        "Svyatoslav",
                        "Timofei",
                        "Vadim",
                        "Seryoga",
                        "Valentin",
                        "Valeriy",
                        "Vasiliy",
                        "Viktor",
                        "Vitaliy",
                        "Vladimir",
                        "Vladislav",
                        "Vyacheslav",
                        "Yaroslav",
                        "Yegor",
                        "Yevgeniy",
                        "Yuriy"};

    public static String getRandomName() {
        return new String(m_Names[RandomNumberGenerator.randomInt(0, m_Names.length-1)]);
    }
}
