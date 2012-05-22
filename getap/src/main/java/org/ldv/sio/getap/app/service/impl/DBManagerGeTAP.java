package org.ldv.sio.getap.app.service.impl;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.ldv.sio.getap.app.AccPersonalise;
import org.ldv.sio.getap.app.Classe;
import org.ldv.sio.getap.app.DemandeConsoTempsAccPers;
import org.ldv.sio.getap.app.Discipline;
import org.ldv.sio.getap.app.Role;
import org.ldv.sio.getap.app.User;
import org.ldv.sio.getap.app.UserSearchCriteria;
import org.ldv.sio.getap.app.service.IFManagerGeTAP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service("DBServiceMangager")
public class DBManagerGeTAP implements IFManagerGeTAP {

	private static JdbcTemplate jdbcTemplate;

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public List<DemandeConsoTempsAccPers> getAllDCTAPByEleve(User eleve) {
		Long id = eleve.getId();
		return this.jdbcTemplate.query("select * from dctap where idEleve = "
				+ id, new DemandeMapper());
	}

	public List<DemandeConsoTempsAccPers> getAllDCTAPByProfInterv(User profi) {
		Long id = profi.getId();
		return this.jdbcTemplate.query("select * from dctap where idProf = "
				+ id, new DemandeMapper());
	}

	public List<DemandeConsoTempsAccPers> getAllDCTAPByProfPrinc(User profp) {
		Long id = profp.getId();
		return this.jdbcTemplate.query("select * from dctap where idProf = "
				+ id, new DemandeMapper());
	}

	public List<DemandeConsoTempsAccPers> getAllDCTAPByClasse(String nomClasse) {
		return this.jdbcTemplate
				.query("select * from dctap d, user u, classe c where d.idEleve = u.id and u.idClasse = c.id and libelle = 'nomClasse' ",
						new DemandeMapper());
	}

	public int getAllDCTAPByEtat(int etat, Long id) {
		int count = this.jdbcTemplate
				.queryForInt(
						"select count(id) from dctap where Etat = ? and (idProf = ? or idEleve = ?)",
						new Object[] { etat, id, id });
		return count;
	}

	public DemandeConsoTempsAccPers getDCTAPById(Long id) {
		return this.jdbcTemplate.queryForObject(
				"select * from dctap where id = ?", new Object[] { id },
				new DemandeMapper());
	}

	public void addDCTAP(DemandeConsoTempsAccPers dctap) {
		String anneeScolaire = dctap.getAnneeScolaire();
		Date dateAction = dctap.getDateAction();
		int dureeAP = dctap.getMinutes();
		int etat = dctap.getEtat();
		Long idProf = dctap.getProf().getId();
		Long idEleve = dctap.getEleve().getId();
		int idAP;
		if (dctap.getAccPers().getId() != null) {
			idAP = dctap.getAccPers().getId();
		} else {
			idAP = this.getAPByNom(dctap.getAccPers().getNom()).getId();
		}

		this.jdbcTemplate
				.update("insert into dctap(anneeScolaire, dateAction, dureeAP, Etat, idProf, idEleve, idAP) values(?,?,?,?,?,?,?)",
						new Object[] { anneeScolaire, dateAction, dureeAP,
								etat, idProf, idEleve, idAP });

	}

	public void updateDCTAP(DemandeConsoTempsAccPers dctap) {
		Long id = dctap.getId();
		String anneeScolaire = dctap.getAnneeScolaire();
		Date dateAction = dctap.getDateAction();
		int dureeAP = dctap.getMinutes();
		int etat = dctap.getEtat();
		Long idProf = dctap.getProf().getId();
		Long idEleve = dctap.getEleve().getId();
		int idAP = dctap.getAccPers().getId();

		this.jdbcTemplate
				.update("update dctap set anneeScolaire = ?, dateAction = ?, dureeAP = ?, Etat = ?, idProf = ?, idEleve = ?, idAP = ? where id = ?",
						new Object[] { anneeScolaire, dateAction, dureeAP,
								etat, idProf, idEleve, idAP, id });

	}

	public void deleteDCTAP(DemandeConsoTempsAccPers dctap) {
		Long id = dctap.getId();
		this.jdbcTemplate.update("delete from dctap where id = ?",
				new Object[] { id });

	}

	public boolean deleteDCTAPById(Long id) {
		int result = this.jdbcTemplate
				.queryForInt("select count(id) from dctap where id = ?",
						new Object[] { id });
		if (result == 0)
			return false;
		else
			return true;
	}

	public List<User> getAllProf() {
		return this.jdbcTemplate.query(
				"select * from user where role like 'prof%'", new UserMapper());

	}

	public List<User> getAllProfInter() {
		return this.jdbcTemplate.query(
				"select * from user where role = 'prof-intervenant'",
				new UserMapper());
	}

	public List<User> getAllProfPrinc() {
		return this.jdbcTemplate.query(
				"select * from user where role = 'prof-principal'",
				new UserMapper());
	}

	public List<User> getAllEleve() {
		return this.jdbcTemplate.query(
				"select * from user where role = 'eleve'", new UserMapper());
	}

	public User getUserById(Long id) {
		User user;
		try {
			user = this.jdbcTemplate.queryForObject(
					"select * from user where id = ?", new Object[] { id },
					new UserMapper());

		} catch (EmptyResultDataAccessException e) {
			user = null;
		}
		return user;
	}

	public void addUser(User user) {
		String nom = user.getNom();
		String prenom = user.getPrenom();
		String login;
		if ((user.getPrenom().charAt(0) + user.getNom()).length() >= 6) {
			login = (user.getPrenom().charAt(0) + user.getNom()).toLowerCase();
		} else if ((user.getPrenom().charAt(0) + user.getNom()).length() == 5) {
			login = (user.getPrenom().charAt(0) + "_" + user.getNom())
					.toLowerCase();
		} else if ((user.getPrenom() + user.getNom()).length() < 6) {
			login = (user.getPrenom() + "_" + user.getNom()).toLowerCase();

		} else {
			login = (user.getPrenom() + user.getNom()).toLowerCase();
		}
		if (login.length() > 10) {
			login = login.substring(0, 10);
		}
		if (login.contains('é' + "") || login.contains('è' + "")) {
			login = login.replace('é', 'e');
			login = login.replace('è', 'e');
		}
		if (login.contains('à' + "") || login.contains('â' + "")) {
			login = login.replace('à', 'a');
			login = login.replace('â', 'a');
		}
		if (login.contains("'" + "")) {
			login = login.replace("'", "");
		}
		String mail = user.getMail();
		try {
			User user2 = this.jdbcTemplate
					.queryForObject(
							"select * from user where login like "
									+ "'"
									+ login
									+ "%'"
									+ " and nom = ? and prenom = ? order by id desc limit 0,1",
							new Object[] { nom, prenom }, new UserMapper());

			if (user2 != null) {
				int max = 2;
				String log = user2.getLogin();
				String sNb = log.charAt(log.length() - 1) + "";

				if (isInteger(sNb)) {
					int nb = Integer.parseInt(sNb);
					max = nb + 1;
				}
				String sMax = String.valueOf(max);
				login += sMax;
			}
		} catch (EmptyResultDataAccessException e) {

		}
		String mdp = generate(5);
		String role = user.getRole();
		int classe = user.getClasse().getId();

		User user3 = this.jdbcTemplate
				.queryForObject(
						"select * from user where login = ? and mdp = ? order by id desc limit 0,1",
						new Object[] { login, mdp }, new UserMapper());

		if (role.equals("prof-principal")) {
			this.jdbcTemplate
					.update("insert into user(nom,prenom,login,mdp,role,idClasse, mail) values(?,?,?,?,?,?,?)",
							new Object[] { nom, prenom, login, mdp, role, null,
									mail });
			Long idUser = user3.getId();

			for (int i = 0; i < user.getLesClasses().length; i++) {
				this.jdbcTemplate
						.update("insert into prof_principal(idUser,idClasse) values(?,?)",
								new Object[] { idUser, user.getLesClasses()[i] });
			}
		} else {
			this.jdbcTemplate
					.update("insert into user(nom,prenom,login,mdp,role,idClasse, mail) values(?,?,?,?,?,?,?)",
							new Object[] { nom, prenom, login, mdp, role,
									classe, mail });
		}

		if (role.startsWith("prof")) {
			this.jdbcTemplate
					.update("update user set idDiscipline = ? where id = ?",
							new Object[] { user.getDiscipline().getId(),
									user3.getId() });
		}

	}

	public boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	public String generate(int length) {
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-";
		String pass = "";
		for (int x = 0; x < length; x++) {
			int i = (int) Math.floor(Math.random() * 64); // Si tu supprimes des
															// lettres tu
															// diminues ce nb
			pass += chars.charAt(i);
		}
		return pass;
	}

	public void updateUser(User user) {
		Long id = user.getId();
		String nom = user.getNom();
		String prenom = user.getPrenom();
		String role = user.getRole();
		int idClasse = 0;
		if (role.equals("eleve")) {
			idClasse = user.getClasse().getId();
		}
		String login = user.getLogin();
		String pass = user.getPass();
		String mail = user.getMail();
		int dis = 0;
		if (role.startsWith("prof")) {
			dis = user.getDiscipline().getId();
		}

		this.jdbcTemplate.update("delete from prof_principal where idUser = ?",
				new Object[] { id });

		if (role.equals("prof-principal")) {
			for (int i = 0; i < user.getLesClasses().length; i++) {
				this.jdbcTemplate
						.update("insert into prof_principal(idUser,idClasse) values(?,?)",
								new Object[] { id, user.getLesClasses()[i] });
			}
		}
		if (role.equals("eleve")) {
			this.jdbcTemplate
					.update("update user set nom = ?, prenom = ?, role = ?, idClasse = ?, login = ?, mdp = ?, mail = ?, idDiscipline= ? where id = ?",
							new Object[] { nom, prenom, role, idClasse, login,
									pass, mail, null, id });
		} else if (role.equals("admin")) {
			this.jdbcTemplate
					.update("update user set nom = ?, prenom = ?, role = ?, idClasse = ?, login = ?, mdp = ?, mail = ?, idDiscipline= ? where id = ?",
							new Object[] { nom, prenom, role, null, login,
									pass, mail, null, id });
		} else {
			this.jdbcTemplate
					.update("update user set nom = ?, prenom = ?, role = ?, idClasse = ?, login = ?, mdp = ?, mail = ?, idDiscipline= ? where id = ?",
							new Object[] { nom, prenom, role, null, login,
									pass, mail, dis, id });
		}

	}

	public void deleteUser(User user) {
		Long id = user.getId();

		if (user.getRole().equals("prof-principal")) {
			this.jdbcTemplate
					.update("delete from prof_principal where idUser = ? and idClasse = ?",
							new Object[] { id, user.getClasse().getId() });
		}
		this.jdbcTemplate.update("delete from user where id = ?",
				new Object[] { id });

	}

	public List<AccPersonalise> getAllAP() {
		return this.jdbcTemplate.query("select * from ap", new AccMapper());
	}

	public AccPersonalise getAPById(int id) {
		AccPersonalise acc;
		try {
			acc = this.jdbcTemplate.queryForObject(
					"select * from ap where id = ?", new Object[] { id },
					new AccMapper());
		} catch (EmptyResultDataAccessException e) {
			acc = null;
		}

		return acc;
	}

	public AccPersonalise getAPByNom(String nom) {
		AccPersonalise acc;
		try {
			acc = this.jdbcTemplate.queryForObject(
					"select * from ap where libelle = ?", new Object[] { nom },
					new AccMapper());
		} catch (EmptyResultDataAccessException e) {
			acc = null;
		}

		return acc;
	}

	public void addAP(AccPersonalise ap) {
		String libelle = ap.getNom();
		int origineEtat = ap.getOrigineEtat();
		Long idUser = ap.getIdUser();

		this.jdbcTemplate.update(
				"insert into ap(libelle, origineEtat, idUser) values(?,?,?)",
				new Object[] { libelle, origineEtat, idUser });

	}

	public void upDateAP(AccPersonalise ap) {
		int id = ap.getId();
		String libelle = ap.getNom();
		int origineEtat = ap.getOrigineEtat();

		this.jdbcTemplate
				.update("update ap set libelle = ?, origineEtat = ? where id = ? values(?,?,?)",
						new Object[] { libelle, origineEtat, id });

	}

	public void deleteAP(AccPersonalise ap) {
		int id = ap.getId();
		this.jdbcTemplate.update("delete from ap where id = ?",
				new Object[] { id });
	}

	public List<Discipline> getAllDiscipline() {
		return this.jdbcTemplate.query(
				"select * from discipline order by libelle",
				new DisciplineMapper());
	}

	public List<Classe> getAllClasse() {
		return this.jdbcTemplate.query("select * from classe order by libelle",
				new ClasseMapper());
	}

	public Classe getClasseById(int id) {
		Classe classe;
		try {
			classe = this.jdbcTemplate.queryForObject(
					"select * from classe where id = ?", new Object[] { id },
					new ClasseMapper());
		} catch (EmptyResultDataAccessException e) {
			classe = null;
		}

		return classe;
	}

	public int countClasses() {
		int count = this.jdbcTemplate.queryForInt(
				"select count(id) from classe order by libelle",
				new Object[] {});
		return count;
	}

	public void addClasse(Classe classe) {
		int id = classe.getId();
		String libelle = classe.getNom();
		this.jdbcTemplate.update("insert into classe(id, libelle) values(?,?)",
				new Object[] { id, libelle });

	}

	public void upDateClasse(Classe classe) {
		int id = classe.getId();
		String libelle = classe.getNom();
		this.jdbcTemplate.update("update classe set libelle = ? where id = ?",
				new Object[] { libelle, id });

	}

	public void deleteClasse(Classe classe) {
		int id = classe.getId();
		String libelle = classe.getNom();
		this.jdbcTemplate.update(
				"delete from classe where id = ? and libelle = ?",
				new Object[] { id, libelle });

	}

	public String getCurrentAnneeScolaire() {
		String annee;
		try {
			annee = this.jdbcTemplate.queryForObject(
					"select * from param_annee order by id desc limit 0,1",
					new Object[] {}, new StringMapper());
		} catch (EmptyResultDataAccessException e) {
			annee = null;
		}

		return annee;
	}

	public List<String> getAllAnneeScolaire() {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUserByLogin(String login, String pw) {
		User user;
		try {
			user = this.jdbcTemplate.queryForObject(
					"select * from user where login = ? and mdp = ?",
					new Object[] { login, pw }, new UserMapper());

		} catch (EmptyResultDataAccessException e) {
			user = null;
		}
		return user;
	}

	// classe pour passage d'une ligne d'une table à un objet
	private static final class UserMapper implements RowMapper<User> {
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			User user = new User();
			user.setId(rs.getLong("id"));
			user.setPrenom(rs.getString("prenom"));
			user.setNom(rs.getString("nom"));
			user.setRole(rs.getString("role"));

			DBManagerGeTAP manager = new DBManagerGeTAP();
			Classe classe = manager.getClasseById(rs.getInt("idClasse"));
			Discipline dis = manager.getDisciplineById(rs
					.getInt("idDiscipline"));
			user.setDiscipline(dis);
			user.setClasse(classe);
			user.setLogin(rs.getString("login"));
			user.setPass(rs.getString("mdp"));
			user.setMail(rs.getString("mail"));
			return user;
		}
	}

	private static final class ClasseMapper implements RowMapper<Classe> {
		public Classe mapRow(ResultSet rs, int rowNum) throws SQLException {
			Classe classe = new Classe();
			classe.setId(rs.getInt("id"));
			classe.setNom(rs.getString("libelle"));
			return classe;
		}
	}

	private static final class DisciplineMapper implements
			RowMapper<Discipline> {
		public Discipline mapRow(ResultSet rs, int rowNum) throws SQLException {
			Discipline dis = new Discipline();
			dis.setId(rs.getInt("id"));
			dis.setNom(rs.getString("libelle"));
			return dis;
		}
	}

	private static final class AccMapper implements RowMapper<AccPersonalise> {
		public AccPersonalise mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			AccPersonalise acc = new AccPersonalise();
			acc.setId(rs.getInt("id"));
			acc.setNom(rs.getString("libelle"));
			acc.setOrigineEtat(rs.getInt("origineEtat"));
			acc.setIdUser(rs.getLong("idUser"));
			return acc;
		}
	}

	private static final class DemandeMapper implements
			RowMapper<DemandeConsoTempsAccPers> {
		public DemandeConsoTempsAccPers mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			DemandeConsoTempsAccPers dctap = new DemandeConsoTempsAccPers();
			dctap.setId(rs.getLong("id"));
			dctap.setAnneeScolaire(rs.getString("anneeScolaire"));
			dctap.setDateAction(rs.getDate("dateAction"));
			dctap.setMinutes(rs.getInt("dureeAP"));
			dctap.setEtat(rs.getInt("Etat"));

			Long idProf = rs.getLong("idProf");
			Long idEleve = rs.getLong("idEleve");
			int idAP = rs.getInt("idAP");

			DBManagerGeTAP manager = new DBManagerGeTAP();
			User prof = manager.getUserById(idProf);
			User eleve = manager.getUserById(idEleve);
			AccPersonalise ap = manager.getAPById(idAP);

			dctap.setProf(prof);
			dctap.setEleve(eleve);
			dctap.setAccPers(ap);

			return dctap;
		}
	}

	private static final class StringMapper implements RowMapper<String> {
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			String annee = null;
			annee = rs.getString("anneeScolaire");

			return annee;
		}
	}

	public List<Role> getAllRole() {
		List<Role> listeRoles = new ArrayList<Role>();
		listeRoles.add(new Role(1, "eleve"));
		listeRoles.add(new Role(2, "prof-intervenant"));
		listeRoles.add(new Role(3, "prof-principal"));
		listeRoles.add(new Role(4, "admin"));

		return listeRoles;
	}

	public List<User> search(UserSearchCriteria userSearchCriteria) {
		String query = userSearchCriteria.getQuery();
		return this.jdbcTemplate.query("select * from user where nom like "
				+ "'" + query + "%'", new UserMapper());
	}

	public List<User> searchProf(UserSearchCriteria userSearchCriteria) {
		String query = userSearchCriteria.getQuery();
		return this.jdbcTemplate.query(
				"select * from user where role like 'prof%' and nom like "
						+ "'" + query + "%'", new UserMapper());
	}

	public List<User> searchClasse(UserSearchCriteria userSearchCriteria) {
		String query = userSearchCriteria.getQuery();
		return this.jdbcTemplate.query(
				"select * from user u, classe c where u.idClasse = c.id and c.libelle = "
						+ "'" + query + "'", new UserMapper());
	}

	public List<DemandeConsoTempsAccPers> searchDctap(
			UserSearchCriteria userSearchCriteria) {
		String query = userSearchCriteria.getQuery();
		return this.jdbcTemplate
				.query("select * from user u, dctap d where (u.id = d.idEleve or u.id = d.idProf) and nom like "
						+ "'" + query + "%'", new DemandeMapper());
	}

	public List<DemandeConsoTempsAccPers> searchDctapClasse(
			UserSearchCriteria userSearchCriteria) {
		String query = userSearchCriteria.getQuery();
		return this.jdbcTemplate
				.query("SELECT dctap.* FROM classe, user, dctap  where classe.id=user.idClasse and user.id=dctap.idEleve and classe.libelle = "
						+ "'" + query + "'", new DemandeMapper());
	}

	public User getUser(Long id) {
		User user;
		try {
			user = this.jdbcTemplate.queryForObject(
					"select * from user where id = ?", new Object[] { id },
					new UserMapper());

		} catch (EmptyResultDataAccessException e) {
			user = null;
		}
		return user;
	}

	public Discipline getDisciplineById(int id) {
		Discipline dis;
		try {
			dis = this.jdbcTemplate.queryForObject(
					"select * from discipline where id = ?",
					new Object[] { id }, new DisciplineMapper());

		} catch (EmptyResultDataAccessException e) {
			dis = null;
		}
		return dis;
	}

}
