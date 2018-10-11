package es.uvigo.esei.dai.hybridserver.pages.xmlpages;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import es.uvigo.esei.dai.hybridserver.Configuration;

import java.util.Properties;

public class XMLPagesDB implements XMLPages {

	private String DB_URL;
	private String DB_USER;
	private String DB_PASSWORD;

	public XMLPagesDB(Properties properties) {
		this.DB_URL = properties.getProperty("db.url");
		this.DB_USER = properties.getProperty("db.user");
		this.DB_PASSWORD = properties.getProperty("db.password");
	}

	public XMLPagesDB(Configuration config) {
		this.DB_URL = config.getDbURL();
		this.DB_USER = config.getDbUser();
		this.DB_PASSWORD = config.getDbPassword();
	}

	public String[] getUUIDs() {
		
		List<String> toReturnList = new ArrayList<String>();
		
		// 1. Conexión a la base de datos
		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

			// 2. Creación de la consulta.
			try (Statement statement = connection.createStatement()) {
				// 3. Consulta
				try (ResultSet result = statement.executeQuery("SELECT uuid FROM XML")) {
					while(result.next()) {
						toReturnList.add(result.getString("uuid"));
					}
				}				
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return toReturnList.toArray(new String[0]);
	}

	public String get(String key) {
		// 1. Conexión a la base de datos
		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

			// 2. Creación de la consulta.
			try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM XML WHERE uuid = ?")) {
				// 3. Asignación de los valores
				statement.setString(1, key);

				// 4. Consulta
				ResultSet result = statement.executeQuery();

				// 5. Devolución del resultado
				result.next();
				String toReturn = result.getString("content");
				result.close();
				return toReturn;
			}
		} catch (SQLException e) {
			return null;
		}
	}

	public void put(String key, String value) {
		// 1. Conexión a la base de datos
		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

			// 2. Creación de la consulta.
			try (PreparedStatement statement = connection
					.prepareStatement("INSERT INTO XML (uuid, content) " + "VALUES (?, ?)")) {
				// 3. Asignación de los valores
				statement.setString(1, key);
				statement.setString(2, value);

				// 4. Inserción
				int result = statement.executeUpdate();

				// 5. Comprobación de resultado
				if (result != 1) {
					throw new SQLException("Unexpected result value: " + result);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void remove(String key) {
		// 1. Conexión a la base de datos
		try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

			// 2. Creación de la consulta.
			try (PreparedStatement statement = connection.prepareStatement("DELETE FROM XML WHERE uuid = ?")) {
				// 3. Asignación de los valores
				statement.setString(1, key);

				// 4. Eliminación.
				int result = statement.executeUpdate();

				// 5. Comprobación de resultado
				if (result != 1) {
					throw new SQLException("Unexpected result value: " + result);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
