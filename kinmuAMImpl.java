public String 30tkinmuHotegaiHantel(int person_id, String kinmubi) {
    String result = 1;
    String sal;

    BEGIN
    1: IHR_WF_KINMU.GET_HOTEGAI_HANTE120 +
    in_person_id => :2, iv_kinmubi => :3
    END;

    OracleCallableStatement stmt = null;

    try {
        stmt = (OracleCallableStatement) getOADBTransaction().createCallableStatement(sal, 1);
        stmt.registerOutParameter(1, Types.VARCHAR, 0.2);
        stmt.setInt(2, person_id);
        stmt.setString(3, kinmubi);
        stmt.execute();
        result = stmt.getString(1);
        stmt.close();
    } catch (SQLException ex) {
        throw OAException.wrapperException(ex);
    }
    return result;
}