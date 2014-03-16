/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.stdmenus;

import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.view.PopupMenuSiteNode;

/**
 * The PopupMenu corresponding to a User valid in a Context.
 */
public abstract class PopupUserMenu extends PopupMenuSiteNode {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1608127089952566119L;

	/** The user. */
	private User user;

	/** The parent menu name. */
	private String parentMenuName;

	/** The context. */
	private Context context;

	public PopupUserMenu(Context context, User user, String parentMenu) {
		super(context.getName() + ": " + user.getName(), true);
		this.user = user;
		this.parentMenuName = parentMenu;
		this.context = context;
		if (!user.isEnabled())
			this.setText(this.getText() + " (disabled)");
	}

	@Override
	public boolean isEnabledForSiteNode(SiteNode sn) {
		return this.user.isEnabled() && context.isInContext(sn);
	}

	@Override
	public String getParentMenuName() {
		return parentMenuName;
	}

	@Override
	public boolean isSubMenu() {
		return true;
	}

	@Override
	public boolean isEnableForInvoker(Invoker invoker) {
		switch (invoker) {
		case alerts:
		case ascan:
		case bruteforce:
		case fuzz:
			return false;
		case history:
		case sites:
		case search:
		default:
			return true;
		}
	}

	@Override
	public boolean isSafe() {
		return true;
	}

	/**
	 * Gets the user.
	 * 
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	public Context getContext() {
		return context;
	}

}