package ua.procamp.locking;

public class ProgramQueries {

    public static String SELECT_PROGRAM_QUERY = "select * from programs where id = ?";
    public static String UPDATE_PROGRAM_QUERY = "update programs set (name, version) = (?, ?) where id = ?";

}
