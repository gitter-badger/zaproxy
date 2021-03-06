package org.zaproxy.zap.extension.httpsessions;

import java.util.Locale;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.Cookie;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;

/**
 * The Class HttpSessionsAPI.
 */
public class HttpSessionsAPI extends ApiImplementor {

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(HttpSessionsAPI.class);

	/** The Constant PREFIX defining the name/prefix of the api. */
	private static final String PREFIX = "httpSessions";

	/** The action of creating a new empty session for a site and turns it active. */
	private static final String ACTION_CREATE_EMPTY_SESSION = "createEmptySession";

	/** The action of deleting an existing session. */
	private static final String ACTION_REMOVE_SESSION = "removeSession";

	/** The action of setting a new active session for a site. */
	private static final String ACTION_SET_ACTIVE_SESSION = "setActiveSession";

	/** The action of adding a new session token for a site. */
	private static final String ACTION_ADD_SESSION_TOKEN = "addSessionToken";

	/** The action of removing session token for a site. */
	private static final String ACTION_REMOVE_SESSION_TOKEN = "removeSessionToken";

	/** The action of unsetting a session as active for a site. */
	private static final String ACTION_UNSET_ACTIVE_SESSION = "unsetActiveSession";

	/** The action of setting the value for a session token for a particular session. */
	private static final String ACTION_SET_SESSION_TOKEN = "setSessionTokenValue";

	/** The action of renaming a session. */
	private static final String ACTION_RENAME_SESSION = "renameSession";

	/** The mandatory parameter required for identifying a site to which an action refers. */
	private static final String ACTION_PARAM_SITE = "site";

	/** The mandatory parameter required for identifying a session to which an action refers. */
	private static final String ACTION_PARAM_SESSION = "session";

	/** The mandatory parameter required for identifying a session for renaming. */
	private static final String ACTION_PARAM_SESSION_OLD_NAME = "oldSessionName";

	/** The mandatory parameter required for renaming a session. */
	private static final String ACTION_PARAM_SESSION_NEW_NAME = "newSessionName";

	/** The mandatory parameter required for identifying a session token to which an action refers. */
	private static final String ACTION_PARAM_TOKEN_NAME = "sessionToken";

	/** The mandatory parameter required for setting the value of a session token. */
	private static final String ACTION_PARAM_TOKEN_VALUE = "tokenValue";

	/** The view which describes the current existing sessions for a site. */
	private static final String VIEW_SESSIONS = "sessions";

	/** The view which describes the current active session for a site. */
	private static final String VIEW_ACTIVE_SESSION = "activeSession";

	/** The view which describes which are the session tokens for a particular site. */
	private static final String VIEW_SESSION_TOKENS = "sessionTokens";

	/** The mandatory parameter required for viewing data regarding a particular site. */
	private static final String VIEW_PARAM_SITE = "site";

	/** The mandatory parameter required for viewing data regarding a particular session. */
	private static final String VIEW_PARAM_SESSION = "session";

	/** The extension. */
	private ExtensionHttpSessions extension;

	/**
	 * Instantiates a new http sessions api implementor.
	 * 
	 * @param extension the extension
	 */
	public HttpSessionsAPI(ExtensionHttpSessions extension) {
		super();
		this.extension = extension;

		// Register the actions
		this.addApiAction(new ApiAction(ACTION_CREATE_EMPTY_SESSION, new String[] { ACTION_PARAM_SITE },
				new String[] { ACTION_PARAM_SESSION }));
		this.addApiAction(new ApiAction(ACTION_REMOVE_SESSION, new String[] { ACTION_PARAM_SITE, ACTION_PARAM_SESSION }));
		this.addApiAction(new ApiAction(ACTION_SET_ACTIVE_SESSION, new String[] { ACTION_PARAM_SITE,
				ACTION_PARAM_SESSION }));
		this.addApiAction(new ApiAction(ACTION_UNSET_ACTIVE_SESSION, new String[] { ACTION_PARAM_SITE }));
		this.addApiAction(new ApiAction(ACTION_ADD_SESSION_TOKEN, new String[] { ACTION_PARAM_SITE,
				ACTION_PARAM_TOKEN_NAME }));
		this.addApiAction(new ApiAction(ACTION_REMOVE_SESSION_TOKEN, new String[] { ACTION_PARAM_SITE,
				ACTION_PARAM_TOKEN_NAME }));
		this.addApiAction(new ApiAction(ACTION_SET_SESSION_TOKEN, new String[] { ACTION_PARAM_SITE,
				ACTION_PARAM_SESSION, ACTION_PARAM_TOKEN_NAME, ACTION_PARAM_TOKEN_VALUE }));
		this.addApiAction(new ApiAction(ACTION_RENAME_SESSION, new String[] { ACTION_PARAM_SITE,
				ACTION_PARAM_SESSION_OLD_NAME, ACTION_PARAM_SESSION_NEW_NAME }));

		// Register the views
		this.addApiView(new ApiView(VIEW_SESSIONS, new String[] { VIEW_PARAM_SITE },
				new String[] { VIEW_PARAM_SESSION }));
		this.addApiView(new ApiView(VIEW_ACTIVE_SESSION, new String[] { VIEW_PARAM_SITE }));
		this.addApiView(new ApiView(VIEW_SESSION_TOKENS, new String[] { VIEW_PARAM_SITE }));
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		if (log.isDebugEnabled()) {
			log.debug("Request for handleApiAction: " + name + " (params: " + params.toString() + ")");
		}

		HttpSessionsSite site;
		switch (name) {
		case ACTION_CREATE_EMPTY_SESSION:
			site = extension.getHttpSessionsSite(getAuthority(params.getString(ACTION_PARAM_SITE)), true);
			if (site == null) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SITE);
			}
			final String sessionName = getParam(params, ACTION_PARAM_SESSION, "");
			if ("".equals(sessionName)) {
				site.createEmptySession();
			} else {
				site.createEmptySession(sessionName);
			}
			return ApiResponseElement.OK;
		case ACTION_REMOVE_SESSION:
			site = extension.getHttpSessionsSite(getAuthority(params.getString(ACTION_PARAM_SITE)), false);
			if (site == null) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SITE);
			}
			HttpSession sessionRS = site.getHttpSession(params.getString(ACTION_PARAM_SESSION));
			if (sessionRS == null) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SESSION);
			}
			site.removeHttpSession(sessionRS);
			return ApiResponseElement.OK;
		case ACTION_SET_ACTIVE_SESSION:
			site = extension.getHttpSessionsSite(getAuthority(params.getString(ACTION_PARAM_SITE)), false);
			if (site == null) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SITE);
			}
			String sname = params.getString(ACTION_PARAM_SESSION);
			for (HttpSession session : site.getHttpSessions()) {
				if (session.getName().equals(sname)) {
					site.setActiveSession(session);
					return ApiResponseElement.OK;
				}
			}
			// At this point, the given name does not match any session name
			throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SESSION);
		case ACTION_UNSET_ACTIVE_SESSION:
			site = extension.getHttpSessionsSite(getAuthority(params.getString(ACTION_PARAM_SITE)), false);
			if (site == null) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SITE);
			}
			site.unsetActiveSession();
			return ApiResponseElement.OK;
		case ACTION_ADD_SESSION_TOKEN:
			extension.addHttpSessionToken(getAuthority(params.getString(ACTION_PARAM_SITE)),
					params.getString(ACTION_PARAM_TOKEN_NAME));
			return ApiResponseElement.OK;
		case ACTION_REMOVE_SESSION_TOKEN:
			extension.removeHttpSessionToken(getAuthority(params.getString(ACTION_PARAM_SITE)),
					params.getString(ACTION_PARAM_TOKEN_NAME));
			return ApiResponseElement.OK;
		case ACTION_SET_SESSION_TOKEN:
			site = extension.getHttpSessionsSite(getAuthority(params.getString(ACTION_PARAM_SITE)), false);
			if (site == null) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SITE);
			}
			HttpSession sessionSST = site.getHttpSession(params.getString(ACTION_PARAM_SESSION));
			if (sessionSST == null) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SESSION);
			}
			extension.addHttpSessionToken(getAuthority(params.getString(ACTION_PARAM_SITE)), 
					params.getString(ACTION_PARAM_TOKEN_NAME));
			sessionSST.setTokenValue(params.getString(ACTION_PARAM_TOKEN_NAME),
					new Cookie(null /* domain */, params.getString(ACTION_PARAM_TOKEN_NAME), params.getString(ACTION_PARAM_TOKEN_VALUE)));
			return ApiResponseElement.OK;
		case ACTION_RENAME_SESSION:
			site = extension.getHttpSessionsSite(getAuthority(params.getString(ACTION_PARAM_SITE)), false);
			if (site == null) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SITE);
			}
			if (!site.renameHttpSession(params.getString(ACTION_PARAM_SESSION_OLD_NAME),
					params.getString(ACTION_PARAM_SESSION_NEW_NAME))) {
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						Constant.messages.getString("httpsessions.api.error.rename"));
			}
			return ApiResponseElement.OK;
		default:
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}
	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
		if (log.isDebugEnabled()) {
			log.debug("Request for handleApiView: " + name + " (params: " + params.toString() + ")");
		}

		HttpSessionsSite site;
		switch (name) {
		case VIEW_SESSIONS:
			// Get existing sessions
			site = extension.getHttpSessionsSite(getAuthority(params.getString(ACTION_PARAM_SITE)), false);
			if (site == null) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SITE);
			}

			ApiResponseList response = new ApiResponseList(name);
			String vsName = params.getString(VIEW_PARAM_SESSION);
			// If a session name was not provided
			if (vsName == null || vsName.isEmpty()) {
				Set<HttpSession> sessions = site.getHttpSessions();
				if (log.isDebugEnabled()) {
					log.debug("API View for sessions for " + getAuthority(params.getString(VIEW_PARAM_SITE)) + ": " + site);
				}

				// Build the response
				for (HttpSession session : sessions) {
					// Dont include 'null' sessions
					if (session.getTokenValuesUnmodifiableMap().size() > 0) {
						ApiResponseList sessionResult = new ApiResponseList("session");
						sessionResult.addItem(new ApiResponseElement("name", session.getName()));
						sessionResult.addItem(new ApiResponseSet("tokens", session.getTokenValuesUnmodifiableMap()));
						sessionResult.addItem(new ApiResponseElement("messages_matched", Integer.toString(session
								.getMessagesMatched())));
						response.addItem(sessionResult);
					}
				}
			} // If a session name was provided
			else {
				HttpSession session = site.getHttpSession(vsName);
				if (session != null) {
					ApiResponseList sessionResult = new ApiResponseList("session");
					sessionResult.addItem(new ApiResponseElement("name", session.getName()));
					sessionResult.addItem(new ApiResponseSet("tokens", session.getTokenValuesUnmodifiableMap()));
					sessionResult.addItem(new ApiResponseElement("messages_matched", Integer.toString(session
							.getMessagesMatched())));
					response.addItem(sessionResult);
				}
			}
			return response;

		case VIEW_ACTIVE_SESSION:
			// Get existing sessions
			site = extension.getHttpSessionsSite(getAuthority(params.getString(ACTION_PARAM_SITE)), false);
			if (site == null) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SITE);
			}
			if (log.isDebugEnabled()) {
				log.debug("API View for active session for " + getAuthority(params.getString(VIEW_PARAM_SITE)) + ": " + site);
			}

			if (site.getActiveSession() != null) {
				return new ApiResponseElement("active_session", site.getActiveSession().getName());
			} else {
				return new ApiResponseElement("active_session", "");
			}
		case VIEW_SESSION_TOKENS:
			final String siteName = getAuthority(params.getString(ACTION_PARAM_SITE));
			// Check if the site exists
			if (extension.getHttpSessionsSite(siteName, false) == null) {
				throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, ACTION_PARAM_SITE);
			}
			// Get session tokens
			HttpSessionTokensSet sessionTokens = extension.getHttpSessionTokensSet(siteName);
			ApiResponseList responseST = new ApiResponseList("session_tokens");

			if (sessionTokens != null) {
				Set<String> tokens = sessionTokens.getTokensSet();
				// Build response list
				if (tokens != null) {
					for (String token : tokens) {
						responseST.addItem(new ApiResponseElement("token", token));
					}
				}
			}
			return responseST;
		default:
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
	}
	
	/**
	 * Returns the authority of the given {@code site} (i.e. the host [ ":" port ] ).
	 * <p>
	 * For example, the result of returning the authority from:
	 * <blockquote><pre>http://example.com:8080/some/path?a=b#c</pre></blockquote> is:
	 * <blockquote><pre>example.com:8080</pre></blockquote>
	 * <p>
	 * <strong>Note:</strong> The implementation is optimised to handle only HTTP and HTTPS schemes, the behaviour is undefined
	 * for other schemes.
	 * 
	 * @param site the site whose authority will be extracted
	 * @return the authority of the site
	 */
	static String getAuthority (String site) {
		String authority = site;
		boolean isSecure = false;
		// Remove http(s)://
		if (authority.toLowerCase(Locale.ROOT).startsWith("http://")) {
			authority = authority.substring(7);
		} else if (authority.toLowerCase(Locale.ROOT).startsWith("https://")) {
			authority = authority.substring(8);
			isSecure = true;
		}
		// Remove trailing chrs
		int idx = authority.indexOf('/');
		if (idx > 0) {
			authority = authority.substring(0, idx);
		}
		if (isSecure && authority.indexOf(':') == -1) {
			return authority + ":443";
		}
		return authority;
	}
}
