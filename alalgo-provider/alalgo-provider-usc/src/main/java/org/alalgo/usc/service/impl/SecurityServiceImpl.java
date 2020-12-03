package org.alalgo.usc.service.impl;

import java.util.Date;
import java.util.List;

import org.alalgo.usc.dos.Menu;
import org.alalgo.usc.dos.PermissionDO;
import org.alalgo.usc.dos.RoleDO;
import org.alalgo.usc.dos.UserDO;
import org.alalgo.usc.model.MenuMapper;
import org.alalgo.usc.model.PermissionMapper;
import org.alalgo.usc.model.RoleMapper;
import org.alalgo.usc.model.UserMapper;
import org.alalgo.usc.service.SecurityService;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class SecurityServiceImpl implements SecurityService {
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private RoleMapper roleMapper; 
	@Autowired
	private PermissionMapper permissionMapper; 
	@Autowired
	private MenuMapper menuMapper;
	@Autowired
	private SecurityService securityService;
	
	@Override
	public List<UserDO> getUser(String username,int page,int countPerPage) {
		if(page<=0 || countPerPage<=0)
			throw new IllegalArgumentException();
		
		return userMapper.getUser(username, new RowBounds((page-1)*countPerPage, countPerPage));
	}
	@Override
	public List<RoleDO> getRoleByUserId(Integer userId) {
		return roleMapper.findRoleByUserId(userId);
	}
	@Override
	public List<PermissionDO> getPermissionByUserId(Integer userId) {
		return permissionMapper.findPermissionByUserId(userId);
	}
	@Override
	public void updateUser(UserDO userDO) {
		userMapper.updateUser(userDO);
	}
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	@Override
	public List<Menu> getAllMenu() {
		return menuMapper.findAllMenu();
	}
	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public void updateAll() {
		UserDO userDO = new UserDO();
		userDO.setCreateTime(new Date());
		userDO.setEnable(true);
		userDO.setPassword("1234");
		userDO.setPhoneNumber("123453234");
		userDO.setUpdateTime(new Date());
		userDO.setUserId(233);
		userDO.setUsername("name22");
		userMapper.insertUser(userDO);
		securityService.insertUser(userDO);
	}
	
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	@Override
	public void insertUser(UserDO userDO)  {
		log.debug(">>>insertUser transactionname:" + TransactionSynchronizationManager.getCurrentTransactionName());
		userDO.setUserId(345);
		userMapper.insertUser(userDO);
	}
}
