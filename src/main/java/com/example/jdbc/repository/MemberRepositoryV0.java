package com.example.jdbc.repository;

import com.example.jdbc.connection.DBConnectionUtil;
import com.example.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 * JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {

    public Member save(Member member) throws SQLException{
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null; //파라미터 바인딩을 가능하게 함 (SQL Injection 공격예방)

        try {
            con = getConnection();  // 1.커넥션획득
            pstmt = con.prepareStatement(sql);  // 2. db에 전달할 SQL과 파라미터로 전달할 데이터 준비
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();  // 3. 준비된 sql을 커넥션을 통해 실제 db에 전달
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            // 중요! 리소스 정리는 꼭 해야함. 커넥션이 끊어지지 않을 경우 커넥션 부족으로 장애가 발생 할 수 있음
            close(con, pstmt, null);
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery(); //  데이터를 조회할때는 executeQuery cf) 변경할때 executeUpdate

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    private void close(Connection con, PreparedStatement pstmt, ResultSet rs) {
        if (rs != null){
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if (pstmt != null){
            try {
                pstmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if (con != null){
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }

    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}
