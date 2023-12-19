package edu.miu.cs.cs544.domain;

import edu.miu.cs.cs544.domain.dto.AuditDataDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String userName;

	@Column(length = 60)
	private String userPass;

	private Boolean active = false;

	@Enumerated(EnumType.STRING)
	private RoleType roleType;

	private String email;

	@Embedded
	private AuditData auditData;


	public User(Integer id, String userName, String userPass, Boolean active, AuditData auditData, RoleType roleType) {
		this.id = id;
		this.userName = userName;
		this.userPass = userPass;
		this.active = active;
		this.auditData = auditData;
		this.roleType = roleType;
	}
}
