class TJMediaBackGroundModel{
  //资源图片地址
  var coverUrl = '';
//资源图片
//   @property(nonatomic,strong)UIImage *coverImage;

//资源作者
  var auther = '';

//资源标题
  var title = '';

//资源媒体介绍
  var memo = '';

  double playbackTime = 0.0;
  double playbackDuration = 0.0;

//网络资源地址
  var mediaUrl = '';

//本地资源路径
  var mediaLocalPath = '';

// 是否为无效资源
  bool invalidMedia = false;

//媒体资源id 对应相关文章或者短视频Id
  var mediaId = '';

  TJMediaBackGroundModel(
  this.coverUrl,
  this.auther,
  this.title,
  this.memo ,
  this.playbackTime ,
  this.playbackDuration ,
  this.mediaUrl ,
  this.mediaLocalPath ,
  this.invalidMedia,
  this.mediaId);

  static TJMediaBackGroundModel mapToModel(Map map){
    TJMediaBackGroundModel model = TJMediaBackGroundModel(
        map['coverUrl'] != null?map['coverUrl']:'',
        map['auther'] != null?map['auther']:'',
        map['title'] != null?map['title']:'',
        map['memo'] != null?map['memo']:'',
        double.parse(map['playbackTime'] != null?map['playbackTime']:'0.0'),
        double.parse(map['playbackDuration'] != null?map['playbackDuration']:'0.0'),
        map['mediaUrl'] != null?map['mediaUrl']:'',
        map['mediaLocalPath'] != null?map['mediaLocalPath']:'',
        false,
        map['mediaId'] != null?map['mediaId']:'',);
    return model;
  }

  static List<Map> toMapList(List<TJMediaBackGroundModel> list){
    List<Map> res = [];
    for(var i =0 ;i<list.length;i++){
      TJMediaBackGroundModel model = list[i];
      res.add(model.toMap());
    }
    return res;
  }

  Map toMap(){
    return {
      'coverUrl':coverUrl,
      'auther':auther,
      'title':title,
      'memo':memo,
      'playbackTime':playbackTime,
      'playbackDuration':playbackDuration,
      'mediaUrl':mediaUrl,
      'mediaLocalPath':mediaLocalPath,
      'invalidMedia':invalidMedia,
      'mediaId':mediaId,
    };
  }
}