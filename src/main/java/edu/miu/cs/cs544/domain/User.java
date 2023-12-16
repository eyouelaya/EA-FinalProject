package edu.miu.cs.cs544.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String userName;
	
	private String userPass;
	
	private Boolean active;

	@Embedded
	private AuditData auditData;

	@Enumerated(EnumType.STRING)
	private RoleType roleType;
	
}