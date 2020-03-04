package com.dao;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.entity.BoardDTO;

public class BoardDAO {
	DataSource dataFactory;

	public BoardDAO() { // 생성자
		// DataSource 얻기, 커넥션 풀 사용
		try {
			Context ctx = new InitialContext();
			dataFactory = (DataSource) ctx.lookup("java:comp/env/jdbc/Oracle11g");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}// end 생성자

	// 목록보기
	public ArrayList<BoardDTO> list() {
		ArrayList<BoardDTO> list = new ArrayList<BoardDTO>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = dataFactory.getConnection();
			String query = "select num,author,title, content,to_char(writeday,'YYYY/MM/DD')writeday,readcnt,repRoot,repStep,repIndent FROM board1 order by repRoot desc, repStep asc";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				BoardDTO data = new BoardDTO();
				data.setNum(rs.getInt("num"));
				data.setAuthor(rs.getString("author"));
				data.setTitle(rs.getString("title"));
				data.setContent(rs.getString("content"));
				data.setWriteday(rs.getString("writeday"));
				data.setReadcnt(rs.getInt("readcnt"));
				data.setRepRoot(rs.getInt("repRoot"));
				data.setRepStep(rs.getInt("repStep"));
				data.setRepIndent(rs.getInt("repIndent"));

				list.add(data);
			} // end while
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	} // end select

	// 글쓰기
	public void write(BoardDTO dto) {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = dataFactory.getConnection();
			StringBuffer query = new StringBuffer();
			query.append("INSERT INTO board1(num,title,author,content,");
			query.append(" repRoot, repStep, repIndent,passwd) values");
			query.append("(board1_seq.nextval,?,?,?,board1_seq.currval,0,0,?)");
			pstmt = con.prepareStatement(query.toString());
			pstmt.setString(1, dto.getTitle());
			pstmt.setString(2, dto.getAuthor());
			pstmt.setString(3, dto.getContent());
			pstmt.setString(4, dto.getPasswd());
			int n = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (con != null)
					con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} // end finally
	} // end write
	
	// 조회수 1 증가
	public void readCount(String _num) {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = dataFactory.getConnection();
			StringBuffer query = new StringBuffer();
			query.append("UPDATE board1 SET readcnt = readcnt + 1 ");
			query.append("WHERE num=?");
			pstmt = con.prepareStatement(query.toString());
			pstmt.setInt(1, Integer.parseInt(_num));
			pstmt.executeUpdate();
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(pstmt != null)pstmt.close();
				if(con != null)con.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		} // end finally
	} // end readCount
	
	// 글 자세히 보기
	public BoardDTO retrieve(String _num) {
		// 조회수 증가
		readCount(_num);
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		BoardDTO data = new BoardDTO();
		try {
			con = dataFactory.getConnection();
			StringBuffer query = new StringBuffer();
			query.append("SELECT num, author, title, content, ");
			query.append("writeday, readcnt, repRoot, repIndent, ");
			query.append("repStep FROM board1 WHERE num=?");
			pstmt = con.prepareStatement(query.toString());
			pstmt.setInt(1, Integer.parseInt(_num));
			rs = pstmt.executeQuery();
			if(rs.next()) {
				data.setNum(rs.getInt("num"));
				data.setTitle(rs.getString("title"));
				data.setAuthor(rs.getString("author"));
				data.setContent(rs.getString("content"));
				data.setWriteday(rs.getString("writeday"));
				data.setReadcnt(rs.getInt("readcnt"));
			} // end if
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(rs != null)rs.close();
				if(pstmt != null)pstmt.close();
				if(con != null)con.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return data;
	} // end retrieve
	
	//글 수정하기
	public void update(BoardDTO dto) {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = dataFactory.getConnection();
			StringBuffer query = new StringBuffer();
			query.append("update board1 set title = ?, author=?,");
			query.append("content=?,passwd=? where num=?");
			
			pstmt = con.prepareStatement(query.toString());
			pstmt.setString(1, dto.getTitle());
			pstmt.setString(2, dto.getAuthor());
			pstmt.setString(3, dto.getContent());
			pstmt.setString(4, dto.getPasswd());
			pstmt.setInt(5, dto.getNum());
			pstmt.executeUpdate();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(pstmt != null)pstmt.close();
				if(con != null)con.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
	}//end update
	
	// 비밀번호 체크
	public Map<String,String> pwdCheck(String _num,String _mode, String _passwd){
		Map<String, String> map = new HashMap<String, String>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String pwdChk = null;
		try {
			con = dataFactory.getConnection();
			String query = "SELECT passwd FROM board1 WHERE num=?";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(_num));
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				pwdChk = rs.getString("passwd");
			}
			
			if(pwdChk.equals(_passwd)) {
				if(_mode.equals("update")) {
					map.put("resultUrl","updateui.do");
				}else if(_mode.equals("delete")) {
					map.put("resultUrl","delete.do");
				}
			}else {
				map.put("resultUrl","pwdCheckui.do");
				map.put("resultMsg","비밀번호가 일치하지 않습니다.");
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(rs != null)rs.close();
				if(pstmt != null)pstmt.close();
				if(con != null)con.close();
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return map;
	}
}// end class
