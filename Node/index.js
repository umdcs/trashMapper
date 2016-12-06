var express = require('express');
var bodyParser = require('body-parser');
var app = express()

  app.set("port", 4321);

// The next two sections tell bodyParser which content types to
// parse. We are mainly interested in JSON, ut eventually, encoded,
// multipart data may be useful.
app.use(bodyParser.urlencoded({   // support encoded bodies
    extended: true
}));
//app.use(bodyParser.json());  // support json encoded bodies

//app.use(bodyParser.urlencoded({limit: '100000mb', extended: true}));

//app.use(bodyParser.json({limit: '100000mb'}));
  var jsonParser       = bodyParser.json({limit:1024*1024*20, type:'application/json'});
  var urlencodedParser = bodyParser.urlencoded({ extended:true,limit:1024*1024*20,type:'application/x-www-form-urlencoding' })

  app.use(jsonParser);
  app.use(urlencodedParser);

// ///////////////////////////////////////
//
// MongoDB
//
// This loads the JS Node.js functions in the mongoDBFunctions.js file. It
// attempts to provide a sort of interface to some functions useful to this
// example using anonymous functions.
//
// See that file for the details for what is provided.
var mongodb = require('./trashMapperUser.js')();


//var json = '{"pictures":[]}';

var basic = {
    trash:[],
    user_account:[]
}

var logIn={
   user_account:[]
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
	    basic.user_account.push(LogInJsonObject);
	    logIn.user_account.push(LogInJsonObject);
	    res.send(req.body);
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


app.post('/userPassword', function(req, res) {
       console.log('/userPassword POST URI accessed');
       var userFromClient=req.body.user_name;
       var objName="dummy";
       var objPassword;
       var i=0;
       while(i<basic.user_account.length||objName!=userFromClient){
       var obj=basic.user_account[i];
       objName=obj.user_name;
       objPassword=obj.user_password;
       i++;
      }
       console.log(objName);
       res.send(obj);
   });


app.delete('/userData', function(req, res){
    console.log('/userData DELETE URI accessed');
    if(!req.body) return res.sendStatus(400);
    index = req.body.trash_index;
    basic.trash.splice(index, 1);
    split.items.splice(index, 1);
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
    mongodb.insertPicture( pic );
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
        mongodb.insertUsers(jsonObject);
        res.json(req.body);
        });

  app.get('/', function(req, res) {
        // Dump the whole collection for debugging
                  var str = mongodb.printDatabase('users', function(result) {

               	res.send('<HTML><BODY>' + JSON.stringify(result, null, 2) + '</BODY></HTML>');

                  });
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

