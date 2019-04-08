package ua.procamp.locking;

public class ProgramQueries {

    public static String SELECT_PROGRAM_BY_ID_AND_VER_BLOCKING_QUERY = "select * from programs where id = ? and version = ? for update;";
    public static String SELECT_PROGRAM_BY_ID_AND_VER_QUERY = "select * from programs where id = ? and version = ?;";
    public static String SELECT_PROGRAM_BY_ID_QUERY = "select * from programs where id = ?;";
    public static String UPDATE_PROGRAM_QUERY = "update programs set (name, version) = (?, ?) where id = ? and version = ?";

}
