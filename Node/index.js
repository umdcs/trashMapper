var express = require('express');
var bodyParser = require('body-parser');
var app = express()

  app.set("port", 4321);

app.use(bodyParser.urlencoded({limit: '50mb', extended: true}));

app.use(bodyParser.json({limit: '50mb'}));

//var json = '{"pictures":[]}';

var basic = {
    trash:[]
}
var split = {

 items:[]

};

app.get('/seperate',function(req,res){
    console.log('/seperate GET URI accessed');
    res.send(JSON.stringify(split));
});

app.get('/userData', function(req, res) {
       console.log('/userData GET URI accessed');
       res.send(basic);
   });



app.post('/userAccount', function (req, res)
    {
	console.log('/userAccount POST URI accessed');
	if(!req.body) return res.sendStatus(400);
	var usr_name = req.body.user_name;
	var usr_password = req.body.user_password;

        var LogInJsonObject = {
            user_name: usr_name,
	    user_password: usr_password,
	    };
	    basic.trash.push(LogInJsonObject);
	 });

app.post('/userPassword', function(req, res) {
       console.log('/userPassword POST URI accessed');
       var userFromClient=req.body.user_name;
       var objName="dummy";
       var objPassword;
       var i=0;
       while(i<basic.trash.length||objName!=userFromClient){
       var obj=basic.trash[i];
       objName=obj.user_name;
       objPassword=obj.user_password;
       i++;
      }
       console.log(objName);
       res.send(obj);
   });

app.post('/seperate',function(req,res){
	console.log('/seperate POST URI accessed');
	if(!req.body) return res.sendStatus(400);
	var pic = req.body.picture;
	var jsonObject = {
	    picture:pic
	};
	split.items.push(jsonObject);
	res.json(req.body);
    });

app.post('/userData', function (req, res)
    {

        /*if (!req.body) return res.sendStatus(400)
        var tempjson = req.body;
        var obj = JSON.parse(json);
        obj['pictures'].push(tempjson);
        json = JSON.stringify(obj);
        console.log('/userData POST URI accessed');
        res.send(json);*/
	console.log('/userData POST URI accessed');
	if(!req.body) return res.sendStatus(400);
	var usr_name = req.body.user_name;
	var usr_password = req.body.user_password;
	var trash_type = req.body.type_of_trash;
	var latitude = req.body.trash_latitude;
	var longtitude = req.body.trash_longtitude;
	var date = req.body.trash_generate_date;
	var info = req.body.trash_information;
        var jsonObject = {
            user_name: usr_name,
	    user_password: usr_password,
	    type_of_trash: trash_type,
	    trash_latitude: latitude,
	    trash_longtitude: longtitude,
	    trash_generate_date: date,
	    trash_information: info
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
