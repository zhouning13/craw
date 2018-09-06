package com.rubyren;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Craw {

	public static class CrawEntity implements Serializable {
		private static final long serialVersionUID = 1466403565451367121L;
		private String url;
		private String file;
		private boolean isCrawed = false;

		public CrawEntity() {
			super();
		}

		public CrawEntity(String url, String file) {
			super();
			this.url = url;
			this.file = file;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getFile() {
			return file;
		}

		public void setFile(String file) {
			this.file = file;
		}

		public boolean getIsCrawed() {
			return isCrawed;
		}

		public void setIsCrawed(boolean isCrawed) {
			this.isCrawed = isCrawed;
		}

	}

	public static final File metaFile = new File("D:\\develop\\workspace\\craw1\\meta.json");
	public static final String baseUrl = "http://www.xueanquan.com";
	public static final Path basePath = Paths.get("D:\\develop\\workspace\\craw3");
	public static final String[] ignore = new String[] { "www.119.gov.cn", "si.trustutn.org", "www.beian.gov.cn",
			"www.moe.edu.cn", "www.cctf.org.cn", "www.cea.gov.cn", "www.mps.gov.cn", "119.cctv.com",
			"baowei.gzhnc.edu.cn", "edu.china.com", "hzdaily.hangzhou.com.cn", "hznews.hangzhou.com.cn",
			"jingyan.baidu.com", "jrsc.china.com.cn", "lzrb.newssc.org", "newpaper.dahe.cn", "news.66wz.com",
			"news.qq.com", "news.qz828.com", "news.sina.com.cn", "news.xinhuanet.com", "news.zj.com",
			"qjwb.zjol.com.cn", "sichuan.scol.com.cn", "v.ku6.com", "wpa.qq.com", "www.hangzhou.gov.cn",
			"www.hubei.gov.cn", "www.js119.com", "www.legaldaily.com.cn", "www.qnsb.com", "www.sx.xinhuanet.com",
			"www.xjrb.com", "www.zj.xinhuanet.com", "zj.qq.com", "zjnews.zjol.com.cn", "zt.safe61.com",
			"tv.hangzhou.com.cn", "military.china.com", "news.china.com", "auto.china.com.cn", "auto.zj.com",
			"bbs.66wz.com", "bbs.zj.com", "bf.sina.com.cn", "cate.66wz.com", "culture.china.com", "economy.china.com",
			"edu.china.com", "ent.china.com.cn", "epaper.bjd.com.cn", "finance.china.com", "gongyi.sina.com.cn",
			"henan.sina.com.cn", "hznews.hangzhou.com.cn", "net.china.com.cn", "news.china.com.cn",
			"paper.people.com.cn", "v.qq.com", "www.66wz.com", "www.baidu.com", "www.hangzhou.com.cn", "www.qz828.com",
			"www.sogou.com", "www.tencent.com", "www.qq.com", "view.inews.qq.com", "kb.qq.com", "www.china.com.cn",
			"www.jiathis.com", "search.szfw.org", "mba.zj.com", "fc.zj.com", "fang.zj.com", "www.jxnews.com.cn",
			"www.moe.gov.cn", "t.qq.com", "finance.china.com.cn", "q.gxfin.com", "www.adobe.com", "www.cbrc.gov.cn",
			"www.hkex.com.hk", "epaper.stcn.com", "suining.scol.com.cn", "yun.baidu.com", "pan.baidu.com", "", "", "",
			"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" };

	public static final ObjectMapper om = new ObjectMapper();

	public static void main(String[] args) {
		Map<String, CrawEntity> map = null;
		try {
			map = om.readValue(metaFile, new TypeReference<HashMap<String, CrawEntity>>() {
			});
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		LinkedList<String> q = new LinkedList<>();
		for (CrawEntity e : map.values()) {
			if (e.isCrawed) {
				continue;
			}
			try {
				URI u = new URI(e.getUrl());
				if (u.getHost() == null || !u.getHost().endsWith("safetree.com.cn")
						|| !u.getHost().endsWith("xueanquan.com")) {
					e.setIsCrawed(true);
					continue;
				}
			} catch (URISyntaxException e1) {
				continue;
			}
			q.offer(e.getUrl());
		}
		while (true) {
			String u = q.poll();
			if (u == null) {
				return;
			}
			CrawEntity entity = map.get(u);
			if (entity == null) {
				continue;
			}

			downloadPage(entity);
			List<String> urls = anayHref(entity);
			for (String url : urls) {
				if (!map.containsKey(url)) {
					map.put(url, new CrawEntity(url, toFile(url)));
					q.offer(url);
				}
			}
			try {
				System.out.println("saving :\t" + entity.getUrl());
				om.writerWithDefaultPrettyPrinter().writeValue(metaFile, map);
				System.out.println("saved :\t" + entity.getUrl());
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}

	}

	public static String toFile(String url) {
		String t = null;
		try {
			URL u = new URL(url);

			t = u.getProtocol() + url.replaceAll("http://", "/").replaceAll("https://", "/").replaceAll("\\?", "_")
					.replaceAll("/\\((.*)\\)/", "/").replaceAll("\\\\", "/").replaceAll("//", "/");

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return t;

	}

	public static List<String> anayHref(CrawEntity entity) {
		System.out.println("analysising :\t" + entity.getUrl());
		Path path = basePath.resolve(entity.getFile());
		URI u;
		try {
			u = new URI(entity.getUrl());
		} catch (URISyntaxException e1) {
			return null;
		}

		// Path absPath = basePath.relativize(path);
		// Path schema = absPath.subpath(0, 1);
		// Path domain = absPath.subpath(1, 2);
		// Path domainPath = basePath.resolve(schema).resolve(domain);

		List<String> a = new ArrayList<>();
		try {
			Document doc = Jsoup.parse(path.toFile(), "utf-8");
			Elements hrefs = doc.select("a[href]");
			for (Element e : hrefs) {
				String href = e.attr("href").trim().replaceAll("\r", "").replaceAll("\n", "");
				if (StringUtils.isEmpty(href)) {
					continue;
				}
				if (StringUtils.endsWith(href, "/")) {
					continue;
				}
				if (StringUtils.startsWithIgnoreCase(href, "javascript:")) {
					continue;
				}
				if (StringUtils.startsWithIgnoreCase(href, "#")) {
					continue;
				}
				URI t;
				try {
					t = u.resolve(href);
				} catch (Exception e1) {
					try {
						t = u.resolve(URLEncoder.encode(href, "utf8"));
					} catch (Exception e2) {
						e2.printStackTrace();
						continue;
					}
				}
				if (StringUtils.isEmpty(t.getPath())) {
					continue;
				}
				if (u.getHost() == null || !u.getHost().endsWith("safetree.com.cn")
						|| !u.getHost().endsWith("xueanquan.com")) {
					continue;
				}
				a.add(t.toString());
				System.out.println(href + "\t:\t" + t);
			}
			Elements imgs = doc.select("img[src],source[src]");
			for (Element img : imgs) {
				String href = img.attr("src").trim().replaceAll("\r", "").replaceAll("\n", "");
				if (StringUtils.isEmpty(href)) {
					continue;
				}
				if (StringUtils.startsWithIgnoreCase(href, "javascript:")) {
					continue;
				}
				if (StringUtils.startsWithIgnoreCase(href, "#")) {
					continue;
				}
				URI t;
				try {
					t = u.resolve(href);
				} catch (Exception e1) {
					try {
						t = u.resolve(URLEncoder.encode(href, "utf8"));
					} catch (Exception e2) {
						e2.printStackTrace();
						continue;
					}
				}
				URI f = URI.create(t.getScheme() + "://" + t.getHost()
						+ ((t.getPort() <= 0 || t.getPort() == 80) ? "" : ":" + t.getPort()) + t.getRawPath());
				a.add(f.toString());
				System.out.println(href + "\t:\t" + f);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("analysised :\t" + entity.getUrl());
		return a;
	}

	public static void downloadPage(CrawEntity entity) {
		System.out.println("downloading :\t" + entity.getUrl());
		try {
			URI u = new URI(entity.getUrl());
			if (u.getHost() == null || !u.getHost().endsWith("safetree.com.cn")
					|| !u.getHost().endsWith("xueanquan.com")) {
				entity.setIsCrawed(true);
				return;
			}
		} catch (URISyntaxException e1) {
			entity.setIsCrawed(true);
			return;
		}

		InputStream in = null;
		OutputStream out = null;
		try {
			File f = basePath.resolve(entity.getFile()).toFile();
			if (f.exists() && f.length() > 0) {
				entity.setIsCrawed(true);
				return;
			}

			if (!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			if (!f.exists()) {
				f.createNewFile();
			}

			CloseableHttpClient httpCilent2 = HttpClients.createDefault();
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(600000) // 设置连接超时时间
					.setConnectionRequestTimeout(600000) // 设置请求超时时间
					.setSocketTimeout(600000).setRedirectsEnabled(true)// 默认允许自动重定�?
					.build();
			HttpGet httpGet2 = new HttpGet(entity.getUrl());
			httpGet2.addHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0");
			httpGet2.setConfig(requestConfig);

			HttpResponse httpResponse = httpCilent2.execute(httpGet2);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				in = new BufferedInputStream(httpResponse.getEntity().getContent());
				out = new BufferedOutputStream(new FileOutputStream(f));
				IOUtils.copyLarge(in, out);
				entity.setIsCrawed(true);
			} else {
				// .............
			}

		} catch (FileNotFoundException e) {
			System.out.println("404:\t" + entity.getUrl());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
		System.out.println("downloaded :\t" + entity.getUrl());
	}
}
