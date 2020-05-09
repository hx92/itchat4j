package cn.zhouyafeng.itchat4j.demo.demo1;

import cn.zhouyafeng.itchat4j.api.MessageTools;
import cn.zhouyafeng.itchat4j.api.WechatTools;
import cn.zhouyafeng.itchat4j.beans.BaseMsg;
import cn.zhouyafeng.itchat4j.beans.RecommendInfo;
import cn.zhouyafeng.itchat4j.core.Core;
import cn.zhouyafeng.itchat4j.face.IMsgHandlerFace;
import cn.zhouyafeng.itchat4j.utils.MyHttpClient;
import cn.zhouyafeng.itchat4j.utils.enums.MsgTypeEnum;
import cn.zhouyafeng.itchat4j.utils.enums.StorageLoginInfoEnum;
import cn.zhouyafeng.itchat4j.utils.tools.CommonTools;
import cn.zhouyafeng.itchat4j.utils.tools.DownloadTools;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 * 简单示例程序，收到文本信息自动回复原信息，收到图片、语音、小视频后根据路径自动保存
 *
 * @author https://github.com/yaphone
 * @version 1.0
 * @date 创建时间：2017年4月25日 上午12:18:09
 */
public class SimpleDemo implements IMsgHandlerFace {

  Logger LOG = Logger.getLogger(SimpleDemo.class);

  @Override
  public String textMsgHandle(BaseMsg msg) {
    // String docFilePath = "D:/itchat4j/pic/1.jpg"; // 这里是需要发送的文件的路径
    if (!msg.isGroupMsg()) { // 群消息不处理
      // String userId = msg.getString("FromUserName");
      // MessageTools.sendFileMsgByUserId(userId, docFilePath); // 发送文件
      // MessageTools.sendPicMsgByUserId(userId, docFilePath);
      String text = msg.getText(); // 发送文本消息，也可调用MessageTools.sendFileMsgByUserId(userId,text);
      JSONObject jsonObject = Core.getInstance().getUserInfoMap().get(msg.getFromUserName());
      if (jsonObject != null) {
        String nickName = jsonObject.getString("NickName");
        String remarkName = jsonObject.getString("RemarkName");
        LOG.info(nickName + ":" + text);
        if ("wo".equals(remarkName) && "me".equals(nickName)) {
          if (text.equals("logout")) {
            WechatTools.logout();
          }
//					if (text.equals("222")) {
//						WechatTools.remarkNameByNickName("yaphone", "Hello");
//					}
          if (text.equals("ginfo")) {
            getGroupInfo();
          } else if (text.equals("dpic")) {
            downloadHeadicons();
          }
          text = text.trim();
          if (text.length() > 1 && (text.endsWith("?")
              || text.endsWith("？")
          )) {
            return text.substring(0, text.length() - 1);
          }
          return text + "?";
        } else if ("xx".equals(nickName) && "女神".equals(jsonObject.getString("RemarkName"))) {
          text = text.trim();
          if (text.length() > 1 && (text.endsWith("?")
              || text.endsWith("？")
          )) {
            return text.substring(0, text.length() - 1);
          }
          return text + "?";
        }
      }
    }
    return null;
  }

  private void downloadHeadicons() {
    String baseUrl = "https://" + core.getIndexUrl(); // 基础URL
    String skey = (String) core.getLoginInfo().get(StorageLoginInfoEnum.skey.getKey());
    LOG.info("开始下载好友头像");
    List<JSONObject> friends = WechatTools.getContactList();
    new File(path).mkdirs();
    for (int i = 0; i < friends.size(); i++) {
      JSONObject friend = friends.get(i);
      String url = baseUrl + friend.getString("HeadImgUrl") + skey;
      // String fileName = friend.getString("NickName");
      String headPicPath = path + File.separator + i + ".jpg";

      HttpEntity entity = myHttpClient.doGet(url, null, true, null);
      try {
        OutputStream out = new FileOutputStream(headPicPath);
        byte[] bytes = EntityUtils.toByteArray(entity);
        out.write(bytes);
        out.flush();
        out.close();

      } catch (Exception e) {
        LOG.info(e.getMessage());
      }

    }
  }

  private void getGroupInfo() {
    // 测试群列表
    CommonTools.writeFile("GroupNickNameList", WechatTools.getGroupNickNameList());
    CommonTools.writeFile("GroupIdList", WechatTools.getGroupIdList());
    CommonTools.writeFile("MemberList", Core.getInstance().getMemberList());
    CommonTools.writeFile("GroupPropertyMap", Core.getInstance().getGroupPropertyMap());
    CommonTools.writeFile("GroupMemeberMap", Core.getInstance().getGroupMemeberMap());
//            System.out.print(WechatTools.getGroupNickNameList());
//            System.out.print(WechatTools.getGroupIdList());
//            System.out.print(Core.getInstance().getGroupMemeberMap());
  }

  private void writeFile(String fileName, Object groupNickNameList) {
    try {
      String format = LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).replace(":", "");
      String s = format + "_" + RandomStringUtils.randomAlphabetic(3) + ".txt";
      Path dir = Paths.get("D:\\mywechatinfo\\" + fileName + s);
      if (!dir.getParent().toFile().exists()) {
        Files.createDirectory(dir.getParent());
      }
      Path write = Files.write(dir, Collections.singleton(JSON.toJSONString(groupNickNameList, true)));
      System.out.println(write);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String picMsgHandle(BaseMsg msg) {
    String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());// 这里使用收到图片的时间作为文件名
    String picPath = "D://itchat4j/pic" + File.separator + fileName + ".jpg"; // 调用此方法来保存图片
    DownloadTools.getDownloadFn(msg, MsgTypeEnum.PIC.getType(), picPath); // 保存图片的路径
    return null;//"图片保存成功";
  }

  @Override
  public String voiceMsgHandle(BaseMsg msg) {
    String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
    String voicePath = "D://itchat4j/voice" + File.separator + fileName + ".mp3";
    DownloadTools.getDownloadFn(msg, MsgTypeEnum.VOICE.getType(), voicePath);
    return null;
  }

  @Override
  public String viedoMsgHandle(BaseMsg msg) {
    String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
    String viedoPath = "D://itchat4j/viedo" + File.separator + fileName + ".mp4";
    DownloadTools.getDownloadFn(msg, MsgTypeEnum.VIEDO.getType(), viedoPath);
    return null;
  }

  @Override
  public String nameCardMsgHandle(BaseMsg msg) {
    return null;
  }

  @Override
  public void sysMsgHandle(BaseMsg msg) { // 收到系统消息
    String text = msg.getContent();
    LOG.info(text);
  }

  @Override
  public String verifyAddFriendMsgHandle(BaseMsg msg) {
    MessageTools.addFriend(msg, true); // 同意好友请求，false为不接受好友请求
    RecommendInfo recommendInfo = msg.getRecommendInfo();
    String nickName = recommendInfo.getNickName();
    String province = recommendInfo.getProvince();
    String city = recommendInfo.getCity();
    String text = "你好，来自" + province + city + "的" + nickName + "， 欢迎添加我为好友！";
    return text;
  }

  @Override
  public String mediaMsgHandle(BaseMsg msg) {
    String fileName = msg.getFileName();
    String filePath = "D://itchat4j/file" + File.separator + fileName; // 这里是需要保存收到的文件路径，文件可以是任何格式如PDF，WORD，EXCEL等。
    DownloadTools.getDownloadFn(msg, MsgTypeEnum.MEDIA.getType(), filePath);
//    return "文件" + fileName + "保存成功";
    return null;
  }

  private static final Core core = Core.getInstance();
  private static final MyHttpClient myHttpClient = core.getMyHttpClient();
  private static final String path = "D://mywechatinfo//head"; // 这里需要设置保存头像的路径

}
