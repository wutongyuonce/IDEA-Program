package indi.wrenn.studentsystem.util;
//����sql���Ĺ�����
public class CreateSql {

	//���ݲ�ѯ���ݡ�ѡ���ѧ�����ﷵ���ض���sql���
	public static String getStudent_Sql(String str,String option){
		String sql = null;
		if("ȫ��".equals(option)){
			sql = "select * from tb_Student" ;
		}else if("ѧ��".equals(option)){
			sql = "select * from tb_Student where Student_Id like '%"+str+"%'";
		}else if("����".equals(option)){
			sql = "select * from tb_Student where Student_Name like '%"+str+"%'";
		}else if("�Ա�".equals(option)){
			sql = "select * from tb_Student where Student_Sex like '%"+str+"%'";
		}else if("�꼶".equals(option)){
			sql = "select * from tb_Student where Grade like '%"+str+"%'";
		}else if("�༶".equals(option)){
			sql = "select * from tb_Student where Classe like '%"+str+"%'";
		}else if("רҵ".equals(option)){
			sql = "select * from tb_Student where Major_Name  like '%"+str+"%'";
		}else if("Ժϵ".equals(option)){
			sql = "select * from tb_Student where Department_Name like '%"+str+"%'";
		}
		return sql;
	}
	
	//��������ѯ��sql��䴴��
	public static String getConditions_Sql(String id,String name,String sex,String grade,String department,String major,String classe){
		StringBuilder sql = new StringBuilder("select * from tb_Student where 1=1");
		if(!id.equals("")){
			sql.append(" and Student_Id like '%" + id + "%'  ");
		}
		if(!name.equals("")){
			sql.append(" and Student_Name like '%" + name + "%'  ");
		}
		if(!sex.equals("")){
			sql.append(" and Student_Sex like '%" + sex + "%'  ");
		}
		if(!grade.equals("")){
			sql.append(" and Grade like '%" + grade + "%'  ");
		}
		if(!department.equals("")){
			sql.append(" and Department_Name like '%" + department + "%'  ");
		}
		if(!major.equals("")){
			sql.append(" and Major_Name like '%" + major + "%'  ");
		}
		if(!classe.equals("")){
			sql.append(" and Classe like '%" + classe + "%'  ");
		}
		
		return sql.toString();
	}
	
	//���ݲ�ѯ���ݡ�ѡ���ѧ�����ﷵ���ض���sql���
		public static String getStudent_Sql(String grade,String major,String str,String option){
			String sql = null;
			if("ȫ��".equals(option)){
				sql = "select * from tb_Student where Grade='"+grade+"' and Major_Name='"+major+"'" ;
			}else if("ѧ��".equals(option)){
				sql = "select * from tb_Student where Student_Id like '%"+str+"%' and Grade='"+grade+"' and Major_Name='"+major+"'" ;
			}else if("����".equals(option)){
				sql = "select * from tb_Student where Student_Name like '%"+str+"%' and Grade='"+grade+"' and Major_Name='"+major+"'" ;
			}else if("�Ա�".equals(option)){
				sql = "select * from tb_Student where Student_Sex like '%"+str+"%' and Grade='"+grade+"' and Major_Name='"+major+"'" ;
			}else if("�꼶".equals(option)){
				sql = "select * from tb_Student where Grade like '%"+str+"%' and Grade='"+grade+"' and Major_Name='"+major+"'" ;
			}else if("�༶".equals(option)){
				sql = "select * from tb_Student where Classe like '%"+str+"%' and Grade='"+grade+"' and Major_Name='"+major+"'" ;
			}
			return sql;
		}
	
	
	
	
}
