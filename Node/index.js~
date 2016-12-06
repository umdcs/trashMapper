var express = require('express');
var bodyParser = require('body-parser');
var app = express()

  app.set("port", 4321);
app.use(bodyParser.urlencoded({limit: '50mb', extended: true}));
app.use(bodyParser.json({limit: '50mb'}));
var basic = {
    trash:[]
}
var split = {
 items:[]
};
app.delete('/userData', function(req, res){
    console.log('/userData DELETE URI accessed');
    if(!req.body) return res.sendStatus(400);
    index = req.body.trash_index;
    basic.trash.splice(index, 1);
    split.items.splice(index, 1);
    res.json(req.body);
});
app.get('/seperate',function(req,res){
    console.log('/seperate GET URI accessed');
    res.send(JSON.stringify(split));
});
app.get('/userData', function(req, res) {
       console.log('/userData GET URI accessed');
       res.send(basic);
   });
app.post('/opinion',function(req,res)
    {
    console.log('/opinion POST URI accessed');
    if(!req.body) return res.sendStatus(400);
    var index = req.body.trash_index;
    var likes = req.body.trash_likes;
    var dislikes = req.body.trash_dislikes;
    split.items[index].trash_likes = likes;
    split.items[index].trash_dislikes = dislikes;
    res.json(req.body);
       });
app.post('/seperate',function(req,res)
    {
    console.log('/seperate POST URI accessed');
    if(!req.body) return res.sendStatus(400);
    var pic = req.body.picture;
    var likes = req.body.trash_likes;
    var dislikes = req.body.trash_dislikes;
    var jsonObject = {
        picture:pic,
        trash_likes: likes,
        trash_dislikes: dislikes
    };
    split.items.push(jsonObject);
    res.json(req.body);
    });
app.post('/userData', function (req, res)
    {
    console.log('/userData POST URI accessed');
    if(!req.body) return res.sendStatus(400);
    var usr_name = req.body.user_name;
    var usr_password = req.body.user_password;
    var trash_type = req.body.type_of_trash;
    var latitude = req.body.trash_latitude;
    var longtitude = req.body.trash_longtitude;
    var date = req.body.trash_generate_date;
    var info = req.body.trash_information;
    var orientation = req.body.trash_orientation;
    index = req.body.trash_index;
        var jsonObject = {
            user_name: usr_name,
        user_password: usr_password,
        type_of_trash: trash_type,
        trash_latitude: latitude,
        trash_longtitude: longtitude,
        trash_generate_date: date,
        trash_information: info,
        trash_orientation: orientation,
    };
    basic.trash.push(jsonObject);
    res.json(req.body);
    });
  app.get('/', function(req, res) {
       res.send('<HTML><HEAD></HEAD><BODY><H1>hello world</H1></BODY></HTML>');
       console.log(req);
       });
app.use(function(req, res, next)
   {
       res.status(404).send('Sorry cant find that!');
   });
app.use(function(err, req, res, next)
   {
       console.error(err.stack);
       res.status(500).send('Internal Server Error message - very strange request came in and we do not know how to handle it!!!');
   });
app.listen(app.get("port"), function ()
      {
          console.log('CS4531 Node Example: Node app listening on port: ', app.get("port"));
      });