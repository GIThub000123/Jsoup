package com.gq.jsoup.book;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import TestMyJsoup.HttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParserHtml {
	private static ExecutorService pool = Executors.newFixedThreadPool(10);
	static Document doc = null;
	static int tempnewsHeadlines = -1;
	static 	int index = 4000;
	static List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();
	public static void main(String[] args) {

		long starTime = System.currentTimeMillis();

		ArrayList<String> bookUrls = new ArrayList<String>();
		//分别抓取：互联网、编程、算法
		bookUrls = downloadBookUrl("算法");
		getBookInfo(bookUrls);

		long endTime = System.currentTimeMillis();
		long Time = endTime - starTime;
		System.out.println("执行耗时 : " + Time + " 毫秒 ");
		System.out.println("执行耗时 : " + Time / 1000f + " 秒 ");
	}

	/**
	 * 抓取每本书的info
	 * 
	 * @param
	 * <String>
	 */

	public static void getBookInfo(ArrayList<String> bookUrls) {
		final SaveBookInfo saveBookInfo = new SaveBookInfo();
		final Map<String, String> cookies = new HashMap<String, String>();
		//book.douban.com
		cookies.put("__utma", "81379588.1625906329.1478780180.1478780180.1478780180.1");
		cookies.put("__utmb", "81379588.1.10.1478780180");
		cookies.put("__utmc", "81379588");
		cookies.put("__utmz", "81379588.1478780180.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
		cookies.put("_pk_id.100001.3ac3", "b8e7b1931da4acd1.1478780181.1.1478780181.1478780181.");
		cookies.put("_pk_ses.100001.3ac3", "*");
		//douban.com
		cookies.put("bid", "MvEsSVNL_Nc");
		//read.douban.com
		cookies.put("_ga", "GA1.3.117318709.1478747468");
		cookies.put("_pk_id.100001.a7dd", "ce6e6ea717cbd043.1478769904.1.1478769904.1478769904.");
		cookies.put("_pk_ref.100001.a7dd", "%5B%22%22%2C%22%22%2C1478769904%2C%22https%3A%2F%2Fbook.douban.com%2"
				+ "Fsubject_search%3Fsearch_text%3D%25E6%258E%25A8%25E8%258D%2590%25E7%25B3%25BB%25E7%25BB%259F%25"
				+ "E5%25AE%259E%25E8%25B7%25B5%26cat%3D1001%22%5D");
		//www.douban.com
		cookies.put("_pk_id.100001.8cb4", "237bb6b49215ebbc.1478749116.2.1478774039.1478749120.");
		cookies.put("_pk_ref.100001.8cb4", "%5B%22%22%2C%22%22%2C1478773525%2C%22https%3A%2F%2Fwww.baidu."
				+ "com%2Flink%3Furl%3DlQ4OMngm1b6fAWeomMO7xq6PNbBlxyhdnHqz9mIYN9-ycRbjZvFb1NQyQ7hqzvI46-WThP"
				+ "6A_Qo7oTQNP-98pa%26wd%3D%26eqid%3Da24e155f0000e9610000000258244a0c%22%5D");

		int count = 0;
		List<Map<String, Integer>> list = getIps();
		for (final String url : bookUrls) {

			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Map<String, Integer> map = random(list);
			String ip = new ArrayList<String>(map.keySet()).get(0);
			int port = map.get(ip);

			System.getProperties().setProperty("http.proxyHost", ip);
			System.getProperties().setProperty("http.proxyPort", new String(port + ""));

			pool.execute(new Runnable() {
				public void run() {
					try {
						Document doc = Jsoup.connect(url)
								.header("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)").cookies(cookies)
								.timeout(3000).get();
						Elements titleElement = doc.getElementsByClass("subject clearfix").select("a");
						Elements scoreElement = doc.select("strong");
						Elements ratingSum = doc.getElementsByClass("rating_sum").select("a").select("span");
						Elements authorElement = doc.getElementById("info").select("span").first().select("a");
						Element pressElement = doc.getElementById("info");

						// 书名
						String title = titleElement.attr("title");
						// 评分
						String score = scoreElement.html();
						// 评价人数
						String rating_sum = ratingSum.html();
						// 作者
						String author = authorElement.html();
						// 出版社
						String press = pressElement.text();
						if (press.indexOf("出版社:") > -1) {
							press = pressElement.text().split("出版社:")[1].split(" ")[1];
						} else {
							press = "";
						}
						// 出版日期
						String date = pressElement.text();
						if (date.indexOf("出版年:") > -1) {
							date = pressElement.text().split("出版年:")[1].split(" ")[1];
						} else {
							date = "";
						}
						// 价格
						String price = pressElement.text();
						if (price.indexOf("定价:") > -1) {
							price = pressElement.text().split("定价:")[1].split(" ")[1];
							if (price.equals("CNY")) {
								price = pressElement.text().split("定价:")[1].split(" ")[2];
							}
						} else {
							price = "";
						}

						System.out.println(title);
						// System.out.println(score);
						// System.out.println(rating_sum);
						// System.out.println(author);
						// System.out.println(press);
						// System.out.println(date);
						// System.out.println(price);
						// 评价人数大于1000插入数据到数据库
						if (!rating_sum.equals("") && Integer.parseInt(rating_sum) >= 1000) {
							String sql = "insert into books values (DEFAULT,'" + title + "', '" + score + "', '"
									+ rating_sum + "', '" + author + "', '" + press + "', '" + date + "', '" + price + "')";
							saveBookInfo.saveBookInfo(sql);
							//System.out.println(++count);
						}
						// 睡眠防止ip被封
						/*try {
							System.out.println("睡眠1秒");
							Thread.currentThread().sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


				}
			});

		}
	}

	/**
	 * 保存书的url
	 * 
	 * @param keyWord
	 * @return
	 */
	public static ArrayList<String> downloadBookUrl(String keyWord) {
		final ArrayList<String> bookUrls = new ArrayList<String>();
		try {
			final Map<String, String> cookies = new HashMap<String, String>();
			//book.douban.com
			cookies.put("__utma", "81379588.1625906329.1478780180.1478780180.1478780180.1");
			cookies.put("__utmb", "81379588.1.10.1478780180");
			cookies.put("__utmc", "81379588");
			cookies.put("__utmz", "81379588.1478780180.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
			cookies.put("_pk_id.100001.3ac3", "b8e7b1931da4acd1.1478780181.1.1478780181.1478780181.");
			cookies.put("_pk_ses.100001.3ac3", "*");
			//douban.com
			cookies.put("bid", "MvEsSVNL_Nc");
			//read.douban.com
			cookies.put("_ga", "GA1.3.117318709.1478747468");
			cookies.put("_pk_id.100001.a7dd", "ce6e6ea717cbd043.1478769904.1.1478769904.1478769904.");
			cookies.put("_pk_ref.100001.a7dd", "%5B%22%22%2C%22%22%2C1478769904%2C%22https%3A%2F%2Fbook.douban.com%2"
					+ "Fsubject_search%3Fsearch_text%3D%25E6%258E%25A8%25E8%258D%2590%25E7%25B3%25BB%25E7%25BB%259F%25"
					+ "E5%25AE%259E%25E8%25B7%25B5%26cat%3D1001%22%5D");
			//www.douban.com
			cookies.put("_pk_id.100001.8cb4", "237bb6b49215ebbc.1478749116.2.1478774039.1478749120.");
			cookies.put("_pk_ref.100001.8cb4", "%5B%22%22%2C%22%22%2C1478773525%2C%22https%3A%2F%2Fwww.baidu."
					+ "com%2Flink%3Furl%3DlQ4OMngm1b6fAWeomMO7xq6PNbBlxyhdnHqz9mIYN9-ycRbjZvFb1NQyQ7hqzvI46-WThP"
					+ "6A_Qo7oTQNP-98pa%26wd%3D%26eqid%3Da24e155f0000e9610000000258244a0c%22%5D");
			final String key = keyWord;
			//for(;index > -1; index += 20){

			// 获取cookies
			List<Map<String, Integer>> list = getIps();
			final CountDownLatch latch = new CountDownLatch(bookUrls.size());
			while (true){
				Map<String, Integer> map = random(list);
				String ip = new ArrayList<String>(map.keySet()).get(0);
				int port = map.get(ip);

				System.getProperties().setProperty("http.proxyHost", ip);
				System.getProperties().setProperty("http.proxyPort", new String(port + ""));

				Thread.sleep(3000);

				final int temp = index;
				pool.execute(new Runnable() {
					public void run() {
						try {
							doc = Jsoup.connect("https://book.douban.com/tag/" + key + "?start=" + temp + "&type=T")
                                    .header("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)").cookies(cookies)
                                    .timeout(3000).get();
							index += 20;
							Elements newsHeadlines = doc.select("ul").select("h2").select("a");
							System.out.println("本页：  " + newsHeadlines.size());

							if(tempnewsHeadlines != 0)
							tempnewsHeadlines = newsHeadlines.size();

							for (Element e : newsHeadlines) {
								System.out.println(e.attr("href"));
								bookUrls.add(e.attr("href"));
							}
							System.out.println("共抓取url个数：" + index);
						} catch (IOException e) {
							e.printStackTrace();
						}finally {
							latch.countDown();
						}
					}
				});
				if (tempnewsHeadlines == 0) {
					System.out.println("end");
					break;
				}
			}
			try{
				latch.await();
				pool.shutdown();
			}catch (InterruptedException e){

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bookUrls;
	}

	private static Map<String, Integer> random(List<Map<String, Integer>> list) {
		return list.get(new Random().nextInt(list.size()));
	}

	private static List<Map<String, Integer>> getIps() {
		final CountDownLatch latch = new CountDownLatch(100);
		for (int j = 100; j < 200; j++) {
			final  int page = j;
			pool.execute(new Runnable() {
				public void run() {
					try{
						String html = HttpClient.get("http://www.xicidaili.com/nn/" + page,"utf-8");
						if (html == null) {
							return ;
						}
						Element table = Jsoup.parse(html).getElementById("ip_list");
						Elements trs = table.select("tr");
						int i = 0;
						for (Element tr : trs) {
							if(i > 0) {
								Map<String, Integer> map = new HashMap<String, Integer>();
								Elements tds = tr.select("td");
								map.put(tds.get(1).text(),Integer.parseInt(tds.get(2).text()));
								list.add(map);
								System.out.println(list.size());
							}
							i++;
						}
					}finally {
						latch.countDown();
					}
				}
			});
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("end..." + list.size());
		return list;
	}
}
