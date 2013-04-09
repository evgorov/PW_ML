/* Customize backbone */
var currentUser;

// set some global defaults for Backbone ajax
Backbone.ajax = function(options){
  var sessionKey = currentUser.get('sessionKey');
  if(!sessionKey){
    setTimeout(_.bind(Backbone.ajax, this, options), 100);
    return null;
  }

  var $spinner = $('[role="loading-spinner"]').show();
  if(typeof options.data === 'undefined') options.data = {};
  if(options.type !== 'GET'){
    delete options.contentType;
    if(typeof options.data === 'string') options.data = JSON.parse(options.data);
    options.data = _.reduce(options.data, function(acc, v, k){
      if(typeof v === 'string'){
        acc[k] = v;
      } else {
        acc[k] = JSON.stringify(v);
      }
      return acc;
    }, {});
    options.processData = true;
  }

  options.data.session_key = sessionKey;
  var originalSuccess = options.success;
  options.success = function(){
    $spinner.hide();
    if(originalSuccess) originalSuccess.apply(this, arguments);
  };

  var originalError = options.error;
  options.error = function(){
    $spinner.hide();
    $('[role="page-error"]').show();
    if(originalError) originalError.apply(this, arguments);
    $.cookie('sessionKey', null);
    $.cookie('username', null);
  };

  return $.ajax.call($, options);
};

// Oh yeah thats dirty, dirty, dirty.
function generateUUID(){
    var d = new Date().getTime();
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = (d + Math.random()*16)%16 | 0;
        d = Math.floor(d/16);
        return (c=='x' ? r : (r&0x7|0x8)).toString(16);
    });
    return uuid;
}

/* Puzzle editor */

var Field = Backbone.Model.extend({

  initialize: function(){
    this.updateField();
    this.on('change:questions', this.updateField, this);
    this.on('change:current', this.updateField, this);
    this.set('errors', []);
  },

  updateField: function(){
    var result = {},
        errors = [],
        width = this.get('width'),
        height = this.get('height');

    // this function writes value into result and setting its type to error
    // if overlapping with other tokens
    var writeResultCheckConflict = function(coord, value){
      var x = parseInt(coord.split(':')[0]),
          y = parseInt(coord.split(':')[1]);
      if(!result[coord] || (result[coord].type === value.type && result[coord].value === value.value)){
        result[coord] = value;
        return true;
      } else {
        result[coord] = { type: 'error', hasHint : value.hasHint };
        return false;
      }
    };

    _(this.get('questions')).each(function(question){
      // Get coordinates for all answer letters.
      var coordinates = this._coordinatesFor(question.answer, question.answer_position);
      coordinates = _(coordinates).map(function(o){
        return [o[0] + question.column, o[1] + question.row, o[2]];
      });

      // Write hint:
      var coord = [question.column, question.row].join(':'),
          current = coord == this.get('current'),
          hasTokensOverBoard = !!_(coordinates).find(function(o){
                                 return (o[0] < 1 || o[0] > width || o[1] < 1 || o[1] > height);
                               }),
          hintValue = {
                        type : 'hint',
                        value: question.question_text,
                        hasHint: true,
                        arrow: question.answer_position,
                        current: current
                      },
          hasErrors = hasTokensOverBoard;

      var e = !writeResultCheckConflict(coord, hintValue);
      hasErrors = hasErrors || e;

      // Write tokens:
      _(coordinates).each(function(o){
         var value = {
           type : hasTokensOverBoard ? 'error' : 'value',
           value: o[2],
           current: current
         };

         var e = !writeResultCheckConflict(o.slice(0,2).join(':'), value);
         hasErrors = hasErrors || e;
      });
      if(hasErrors) errors.push(question);
    }, this);

    this.set('errors', errors);
    this.set('coordinates', result);
  },

   _coordinatesFor: function(answer, position_direction){
    var position = position_direction.split(':')[0],
        direction = position_direction.split(':')[1],
        simple_coords = _(answer).map(function(o, index){ return [index, 0, o]; });
    return this._adjustForPosition(position,
                                   this._adjustForDirection(direction,
                                                            simple_coords));
  },

  _adjustForDirection: function(direction, coords){
    switch(direction){
    case 'top':
      return _(coords).map(function(o){ return [o[1],-o[0], o[2]];});
    case 'left':
      return _(coords).map(function(o){ return [-o[0] , o[1], o[2]];});
    case 'right':
      return _(coords).map(function(o){ return [o[0] , o[1], o[2]];});
    case 'bottom':
      return _(coords).map(function(o){ return [o[1], o[0], o[2]];});
    default:
      throw 'not valid direction';
    }

  },

  _adjustForPosition: function(position, coords){
    switch(position){
    case 'north':
      return _(coords).map(function(o){ return [o[0], o[1] - 1, o[2]];});
    case 'north-east':
      return _(coords).map(function(o){ return [o[0] + 1, o[1] - 1, o[2]];});
    case 'east':
      return _(coords).map(function(o){ return [o[0] + 1, o[1], o[2]];});
    case 'south-east':
      return _(coords).map(function(o){ return [o[0] + 1, o[1] + 1, o[2]];});
    case 'south':
      return _(coords).map(function(o){ return [o[0], o[1] + 1, o[2]];});
    case 'south-west':
      return _(coords).map(function(o){ return [o[0] - 1, o[1] + 1, o[2]];});
    case 'west':
      return _(coords).map(function(o){ return [o[0] - 1, o[1], o[2]];});
    case 'north-west':
      return _(coords).map(function(o){ return [o[0] - 1, o[1] - 1, o[2]];});
    default:
      throw 'not valid position';
    }
  }
});

var Puzzle = Backbone.Model.extend({

  defaults: function(){
    return {
      "id": generateUUID(),
      "questions": [],
      "name": _.uniqueId('Сканворд'),
      "issuedAt": (new Date).toISOString().split('T')[0],
      "base_score": 0,
      "time_given": 60000,
      "height": 10,
      "width": 10
    };
  },

  initialize: function(){
    this.field = new Field({
      questions: this.get('questions'),
      width: this.get('width'),
      height: this.get('height')
    });

    this.on('change:height change:width', function(){
      this.field.set('width', this.get('width'));
      this.field.set('height', this.get('height'));
    }, this);

    this.on('change:questions', function(){
      this.field.set('questions', this.get('questions'));
    }, this);
  },

  addEmptyQuestion: function(x, y){
    if(typeof x == 'undefined' && typeof y == 'undefined'){
      x = 1;
      y = 1;
    }
    var question = {
      "column": x,
      "row": y,
      "question_text": "",
      "answer": "",
      "answer_position": "east:right"
    };
    this.set('questions', this.get('questions').concat([question]));
    this.trigger('questionAdded');
  }
});

var FieldView = Backbone.View.extend({

  tagName: 'div',
  id: "field",

  events: {
    'click .token': 'triggerClickEvent'
  },

  triggerClickEvent: function(e){
    var $token = $(e.target),
        type = $token.attr('data-type'),
        x = $token.attr('data-x'),
        y = $token.attr('data-y');

    this.trigger('tokenClick', { type: type, x: x, y: y });
  },

  initialize: function(){
    this.boxHeight = this.boxWidth = 50;
    this.render();
    this.model.on('change:height change:width', this.render, this);
    this.model.on('change:coordinates', this.updateCoordinates, this);
  },

  updateCoordinates: function(){
    _(this.coordinates).each(function(v, k){
        var x = k.split(':')[0],
            y = k.split(':')[1];
        this.setTokenFromCoordinates(v, x, y);
    }, this);
    return this;
  },

  render: function() {
    var height = this.model.get('height'),
        width = this.model.get('width');
    this.coordinates = {};
    var $container = $('<div>').css({'height': height * this.boxHeight + 'px',
                                     'width': width * this.boxWidth + 'px',
                                     'position': 'relative'
                                    });

    for(var x = 1; x <= width; x++){
      for(var y = 1; y <= height; y++){
        var $token = $('<div>').attr('data-x', x).attr('data-y', y).addClass('token');
        this.coordinates[[x, y].join(':')] = $token;
        this.setTokenFromCoordinates($token, x, y);
        $container.append($token);
      }
    }
    this.$el.html('').append($container);
    return this;
  },

  setTokenFromCoordinates: function($token, x, y){
    var properties = this.model.get('coordinates')[[x,y].join(':')];
    // set attributes
    if(properties){
      _(properties).each(function(v,k) { $token.attr('data-' + k, v); });
    } else {
      $token.attr('data-type', 'empty');
      $token.attr('data-current', 'false');      
    }

    if(properties && (properties.type === 'hint' || properties.hasHint)){
      this.initializeDraggable($token);
    } else {
      this.removeDraggable($token);
    }
  },

  removeDraggable: function($token){
    if(!$token.data('draggable')) return null;
    $token.draggable('destroy');
    return null;
  },

  initializeDraggable: function($token){
    if($token.data('draggable')) return null;
    var self = this;
    $token.draggable({
      containment: 'parent',
      helper: 'clone',
      snap: '.token',
      addClasses: false,
      snapTolerance: 5,
      zIndex: 100,
      stop: function(event, ui){
        self.trigger('tokenMoved', {
                       source: {
                         x: parseInt(ui.helper.attr('data-x')),
                         y: parseInt(ui.helper.attr('data-y'))
                       },
                       destination: {
                         x: Math.round(ui.position.left / self.boxWidth) + 1,
                         y: Math.round(ui.position.top / self.boxHeight) + 1
                       }
                     });
      }
    });
    return null;
  }
});

var PuzzleView = Backbone.View.extend({

  tagName: 'div',
  template: _.template($('#puzzle-editor-template').html()),
  rowTemplate: _.template($('#simple-puzzle-view-template').html()),

  events: {
    'click [role="add_question"]': 'addQuestion',
    'click [role="delete_question"]': 'deleteQuestion',
    'keyup .question *': 'changeQuestion',
    'change .question *': 'changeQuestion',
    'click [role="save-puzzle"]': 'savePuzzle',
    'change [role^="puzzle"]': 'updatePuzzleAtribute',
    'click [role="cancel-puzzle"]': 'cancelPuzzle',
    'focus input, textarea': 'setCurrent',
    'blur input, textarea': 'removeCurrent',
    'click [role="current-arrow"]': 'showArrowPicker',
    'click [role="arrow-control-hover"] [data-arrow-control]': 'selectArrow'
  },

  showArrowPicker: function(e){
    e.preventDefault();
    $(e.target).parent().find('[role="arrow-control-hover"]').toggle();
  },

  selectArrow: function(e){
    e.preventDefault();
    var val = $(e.target).attr('data-arrow-control');
    $(e.target).closest('[role="arrow-control-hover"]').hide();
    $(e.target).closest('[role="question"]').find('[role="arrow-select"]').val(val).change();
    $(e.target).closest('[role="question"]').find('[role="current-arrow"]').attr('data-arrow-control', val);
  },

  setCurrent: function(e){
    var $question = $(e.target).closest('.question'),
        x = $question.find('.position.x').val(),
        y = $question.find('.position.y').val();
    this.model.field.set('current', [x, y].join(':'));
    $question.addClass('current');
  },

  removeCurrent: function(e){
    var $question = $(e.target).closest('.question');
    this.model.field.set('current', '');
    $question.removeClass('current');
  },

  addQuestion: function(){ this.model.addEmptyQuestion(); },

  deleteQuestion: function(e){
    $(e.target).closest('.question').remove();
    this.changeQuestion();
  },

  changeQuestion: function(){
    var self = this;
    var questions = this.$el.find('.question').map(function(){
      var $this = $(this);
      return {
        "column": parseInt($this.find('.position.x').val()),
        "row": parseInt($this.find('.position.y').val()),
        "question_text": $this.find('.hint').val(),
        "answer": $this.find('.answer').val().toLowerCase(),
        "answer_position": $this.find('select').val()
      };
    });
    this.model.set('questions', $.makeArray(questions));

    this.$el.find('.questions .question.current').map(function(){
      var $this = $(this),
          x = $this.find('.position.x').val(),
          y = $this.find('.position.y').val();
      self.model.field.set('current', [x, y].join(':'));
    });
  },

  findQuestion: function(x, y){
    return $('.question').
     filter(function(i, o){ return $(o).find('.position.x').val() == x;}).
     filter(function(i, o){ return $(o).find('.position.y').val() == y;}).
     first();
  },

  cancelPuzzle: function(e){
    if(e && e.preventDefault) e.preventDefault();
    this.trigger('cancel');
    this.hide();
  },

  rotateQuestion: function(x, y){
    var $s = this.findQuestion(x, y).find('select');
    $s.val($s.find(':selected + option').val());
    $s.change();
  },

  moveQuestion: function(e){
    var $question = this.findQuestion(e.source.x, e.source.y);
    $question.find('.position.x').val(e.destination.x);
    $question.find('.position.y').val(e.destination.y);
    $question.find('.position.x').keyup();
 },

  updatePuzzleAtribute: function(){
    var size = this.$el.find('[role="puzzle-size"]').val(),
        name = this.$el.find('[role="puzzle-name"]').val(),
        baseScore = this.$el.find('[role="puzzle-base-score"]').val(),
        timeGiven = this.$el.find('[role="puzzle-time-given"]').val();

    this.model.set('name', name);
    this.model.set('base_score', baseScore);
    this.model.set('time_given', timeGiven);
    this.model.set('width', parseInt(size.split('x')[0]));
    this.model.set('height', parseInt(size.split('x')[1]));
  },

  initialize: function(){
    this.$el.html(this.template());
    $('[role="puzzle-editor"]').empty().append(this.$el);
    this.fieldView = new FieldView({ model: this.model.field, el: this.$el.find('[role="field"]')[0] });
    this.fieldView.on('tokenClick', function(e){
                   switch(e.type){
                   case 'empty':
                     this.model.addEmptyQuestion(e.x, e.y);
                     break;
                   case 'hint':
                     this.rotateQuestion(e.x, e.y);
                     break;
                   }
                 }, this);
    this.render();
    this.fieldView.on('tokenMoved', this.moveQuestion, this);
    this.model.on('questionAdded', this.render, this);
    this.model.field.on('change:errors', this.showErrors, this);
  },

  showErrors: function(){
    this.$el.find('.question').removeClass('error');
    _(this.model.field.get('errors')).each(function(error){
       this.findQuestion(error.column, error.row).addClass('error');
    }, this);
  },

  savePuzzle: function(){
    if(this.model.field.get('errors').length == 0) this.hide();
  },

  render: function() {
    var $questions = this.$el.find('.questions'),
        fieldSize = this.model.get('height') + 'x' + this.model.get('width');

    this.$el.find('[role="puzzle-size"]').val(fieldSize);
    this.$el.find('[role="puzzle-name"]').val(this.model.get('name'));
    this.$el.find('[role="puzzle-base-score"]').val(this.model.get('base_score'));
    this.$el.find('[role="puzzle-time-given"]').val(this.model.get('time_given'));

    $questions.empty();
    _(this.model.get('questions')).each(function(question){
      var $q = $(this.rowTemplate(question));
      $q.find('select').val(question.answer_position);
      $q.find('[role="current-arrow"]').attr('data-arrow-control', question.answer_position);
      $questions.append($q);
    }, this);

    return this;
  },

  hide: function(){
    this.$el.parent().hide('fast');
  },

  show: function(){
    var coord = this.$el.parent().offset();
    coord.top = $(document).scrollTop();
    this.$el.parent().offset(coord);
    this.$el.parent().show('fast');
  }
});

/* Signin Form */

var CurrentUser = Backbone.Model.extend({
  initialize: function(){
    if($.cookie('sessionKey')){
      this.set('signed', true);
      this.set('sessionKey',$.cookie('sessionKey'));
      this.set('username', $.cookie('username'));
    }
    this.on('change:signed', function(){
      if(this.get('signed')){
        $.cookie('sessionKey', this.get('sessionKey'), { expires: 7 });
        $.cookie('username', this.get('username'), { expires: 7 });
      } else {
        $.cookie('sessionKey', null);
        $.cookie('username', null);
      }
    }, this);
  },

  logout: function(){
    this.set('sessionKey', null);
    this.set('username', null);
    this.set('signed', false);
  },

  login: function(email, password){
    var self = this;
    $.ajax({
      type: "POST",
      url: '/login',
      cache: false,
      data: { email: email, password: password }
    }).done(function(data) {
      self.set('sessionKey', data.session_key);
      self.set('username', data.me.name);
      self.set('signed', true);
    }).error(function(error){
      if(error.status === 403){
        self.set('message', 'Неправилильный логин или пароль.');
      } else {
        self.set('message', 'Ошибка на сервере.');
      }
    });
  }
});

var LogoutView = Backbone.View.extend({
  tagName: "li",
  className: "pull-right logout",
  events: {
    'click [role="logout-button"]': 'logout'
  },

  initialize: function(){
    this.$el.attr('role', 'logout');
    this.model.on('change:username', function(){
      this.setUsername(this.model.get('username'));
    }, this);
    this.setUsername(this.model.get('username'));
  },

  logout: function(){
    this.model.logout();
  },

  setUsername: function(username){
    if(username){
      this.$el.find('[role="logout-username"]').text(username);
    } else {
      this.$el.find('[role="logout-username"]').text('');
    }
  }
});

var FormSigninView = Backbone.View.extend({
  tagName: 'div',
  className: "form-signin-container",

  events: {
    'submit': 'signin',
    'focus *': 'removeError'
  },

  initialize: function(){
    this.$el.attr('role', 'form-signin-container');
    this.model.on('change:signed', function(){
      if(this.model.get('signed')){
        this.hide();
      } else {
        this.show();
      }
    }, this);
    this.model.on('change:message', function(){
      var message = this.model.get('message');
      if(message){
        this.showError(message);
      } else {
        this.hideError();
      }

    }, this);

    if(this.model.get('signed')) this.hide();
  },

  signin: function(e){
    if(e && e.preventDefault) e.preventDefault();
    var self = this;
    var $email = this.$el.find('[role="form-signin-email"]'),
        $password = this.$el.find('[role="form-signin-password"]');
    this.model.login($email.val(), $password.val());
    setTimeout(function(){
      $email.val('');
      $password.val('');
    }, 1000);
  },

  removeError: function(){
    this.model.set('message', null);
  },

  showError: function(message){
    this.$el.find('[role="form-signin-error"]').text(message).show('fast');
  },

  hideError: function(message){
    this.$el.find('[role="form-signin-error"]').hide('fast');
  },

  hide: function(){
    this.$el.hide('fast');
  },

  show: function(){
    this.$el.show('fast');
  }
});


/* Puzzle sets */

var PuzzleSet = Backbone.Model.extend({
  urlRoot: '/sets',
  defaults: function(){
    return {
      published: false,
      puzzles: [],
      type: '',
      month: ((new Date).getMonth() + 1),
      year: (new Date).getFullYear(),
      name: _.uniqueId('Сет ')
    };
  },

  pushState: function(){
    this.stateHistory = this.stateHistory || [];
    this.stateHistory.push(this.toJSON());
  },

  popState: function(){
    var state = this.stateHistory.pop();
    this.set(state);
  },

  clearState: function(){
    this.stateHistory = [];
  },

  addNewPuzzle: function(){
    var puzzle = (new Puzzle()).toJSON();
    this.set('puzzles', this.get('puzzles').concat([puzzle]));
  },
  deletePuzzle: function(id){
    var newPuzzles = _(this.get('puzzles')).reject(function(o){return o.id == id;});
    this.set('puzzles', newPuzzles);
  },
  getPuzzle: function(id){
    var puzzle = new Puzzle(_(this.get('puzzles')).find(function(o){return o.id == id;}));
    puzzle.on('change', function(){
      var newPuzzles = _(this.get('puzzles')).map(function(o){
        if(o.id == id){
          return puzzle.toJSON();
        } else {
          return o;
        }
      });
      this.set('puzzles', newPuzzles);
    }, this);
    return puzzle;
  }
});

var PuzzleSets = Backbone.Collection.extend({
  model: PuzzleSet,
  url: '/sets',
  comparator: false,
  parse: function(response) {
    return response.sets;
  }
});

var PuzzleSetView = Backbone.View.extend({
  className: 'row m-bottom-20px',
  showTemplate: _.template($('#set-view-template').html()),
  editTemplate: _.template($('#set-view-editor-template').html()),
  events: {
    'click [role="edit-set"]': 'startEditing',
    'click [role="add-puzzle"]': 'addPuzzle',
    'change [role="set-name"]': 'changeName',
    'change [role="set-type"]': 'changeType',
    'click [role="delete-puzzle"]': 'deletePuzzle',
    'click [role="delete-set"]': 'deletePuzzleSet',
    'click [role="edit-puzzle"]': 'editPuzzle',
    'click [role="save-set"]': 'saveSet',
    'click [role="publish-set"]': 'publishSet',
    'click [role="cancel-set"]': 'cancelSet'
  },

  initialize: function(){
    this.editing = false;
    this.model.on('change:puzzles', this.render, this);
  },

  render: function(){
    var template;
    if(this.editing){
      template = this.editTemplate;
    } else {
      template = this.showTemplate;
    }

    this.$el.empty().append(template(this.model.toJSON()));
    this.$el.find('[role="set-type"]').val(this.model.get('type'));
    return this;
  },

  startEditing: function(){
    this.editing = true;
    this.model.pushState();
    this.render();
  },

  addPuzzle: function(e){
    if(e && e.preventDefault) e.preventDefault();
    this.model.addNewPuzzle();
  },

  changeName: function(e){
    this.model.set('name', $(e.target).val());
    return true;
  },

  changeType: function(e){
    this.model.set('type', $(e.target).val());
    return true;
  },

  deletePuzzle: function(e){
    if(e && e.preventDefault) e.preventDefault();
    var id = $(e.target).closest('[role="puzzle-list-item"]').attr('data-puzzle-id');
    this.model.deletePuzzle(id);
  },

  deletePuzzleSet: function(e){
    if(e && e.preventDefault) e.preventDefault();
    this.model.destroy();
    this.remove();
  },

  editPuzzle: function(e){
    if(e && e.preventDefault) e.preventDefault();
    this.model.pushState();
    var id = $(e.target).closest('[role="puzzle-list-item"]').attr('data-puzzle-id'),
        puzzle = this.model.getPuzzle(id);
    puzzleView = new PuzzleView({ model: puzzle });
    puzzleView.on('cancel', function(){
                    this.model.popState();
                    this.render();
                  }, this);
    puzzleView.show();
  },

  cancelSet: function(e){
    if(e && e.preventDefault) e.preventDefault();
    this.editing = false;
    this.model.popState();
    this.render();
  },

  saveSet: function(e){
    if(e && e.preventDefault) e.preventDefault();
    this.model.save();
    this.editing = false;
    this.render();
    this.model.clearState();
  },

  publishSet: function(e){
    if(e && e.preventDefault) e.preventDefault();
    this.model.set('published', true);
    this.model.save();
    this.editing = false;
    this.render();
    this.model.clearState();
  }
});

var PuzzleSetsView = Backbone.View.extend({
  tagName: 'div',
  emptySetsTemplate: _.template($('#set-view-empty-template').html()),
  events: {
    'changeMonth': 'changeMonth',
    'click [role="add-set"]': 'addNewSet'
  },

  initialize: function(){
    this.$el.attr('role', 'sets');
    this.collection.on('reset', this.render, this);

    $('[role="sets-datepicker"]').datepicker({
      format: 'MM yyyy',
      startView: 1,
      language: 'ru'
    });
    this.changeMonth({ 'date': new Date() });
  },

  render: function(){
    var $setsContainer = this.$el.find('[role="sets-container"]');
    if(this.collection.size() === 0){
      $setsContainer.empty().append(this.emptySetsTemplate());
    } else {
      $setsContainer.empty();
      this.collection.each(this.addSet, this);
    }
    return this;
  },

  addNewSet: function(){
    var set = new PuzzleSet(),
        date = this.$el.find('[role="sets-datepicker"]').data('datepicker').date;
    set.set('month', date.getMonth() + 1);
    set.set('year', date.getFullYear());
    this.addSet(set, true);
  },

  addSet: function(set, editing){
    this.$el.find('[role="empty-set"]').remove();
    var view = new PuzzleSetView({ model: set });
    if(editing === true) view.editing = true;
    this.$el.find('[role="sets-container"]').append(view.render().el);
  },

  changeMonth: function(e){
    var dp = this.$el.find('[role="sets-datepicker"]');
    dp.data('datepicker').date = e.date;
    dp.data('datepicker').setValue();
    dp.data('datepicker').hide();
    this.$el.find('[role="current-date-description"]').text(dp.data('date'));
    this.collection.fetch({ data: { year: e.date.getFullYear(),
                                    month: (e.date.getMonth() + 1) } });
  }
});


/* Users tab */

var User = Backbone.Model.extend({});

var Users = Backbone.Collection.extend({
  model: User,
  url: '/users/paginate',
  comparator: false,
  parse: function(response) {
    this.currentPage = response.current_page;
    this.totalPages = response.total_pages;
    return response.users;
  }
});

var UsersView = Backbone.View.extend({
  tagName: 'div',
  rowTemplate: (function(){
              var items = _([
                   '=name', '=surname', 'print(created_at.split(" ")[0])',
                   '=solved', '=month_score', '=high_score', '=hints'
                 ]).map(function(o){return '<td><%' + o + '%></td>';});
               return _.template('<tr>' + items.join('') + '</tr>');
             })(),
  events: {
    'click [role="pagination"] a': 'selectPage'
  },

  initialize: function(){
    this.$el.attr('role', 'users');
    this.collection.on('reset', this.render, this);
    this.collection.fetch();
  },

  render: function(){
    var rows = this.collection.map(function(o){
      return this.rowTemplate(o.toJSON());
    }, this).join('');
    this.$el.find('[role="rows"]').empty().append(rows);
    this.renderPaginator();
    return this;
  },

  renderPaginator: function(){
    var currentPage = this.collection.currentPage,
        totalPages = this.collection.totalPages,
        $result = $('<ul>');
    if(totalPages < 2) return;
    for(var i = 1; i <= totalPages; i++){
      var $li = $('<li>');
      $li.append($('<a>').attr('href', '#').text(i));
      if(i == currentPage) $li.addClass('active');
      $result.append($li);
    }
    this.$el.find('[role="pagination"]').empty().append($result);
  },

  selectPage: function(e){
    if(e && e.preventDefault) e.preventDefault();
    var page = $(e.target).text();
    this.collection.fetch({ data: { page: page }});
  }
});

/* Dashboard */

var Counters = Backbone.Model.extend({
  url: '/counters',
  toData: function(){
    var days = ['День'].concat(this.get('days')),
        logins = ['логины'].concat(this.get('logins')),
        sets_bought = ['покупки сетов'].concat(this.get('sets_bought')),
        hints_bought = ['покупки подсказок'].concat(this.get('hints_bought')),
        scored = ['увеличение счета'].concat(this.get('scored'));
    return _.zip(days, logins, sets_bought, hints_bought, scored);
  }
});


var DashboardView = Backbone.View.extend({

  tagName: 'div',
  className: 'row',

  events: {
  },

  initialize: function(){
    this.counters = new Counters();
    this.counters.on('change', this.renderChart, this);
    this.counters.fetch();
    this.render();
  },

  renderChart: function(){
    var data = google.visualization.arrayToDataTable(this.counters.toData());
    var options = {
      chartArea: { left: 40, width: 900 },
      legend: { position: 'top' }
    };

    var chart = new google.visualization.LineChart(this.$el.find('[role="chart"]')[0]);
    chart.draw(data, options);
  },

  render: function() {}
});


/* Service message */

var ServiceMessage = Backbone.Model.extend({
  url: '/service_messages',
  isNew: function(){ return false; }
});

var ServiceMessageView = Backbone.View.extend({
  tagName: 'div',

  events: {
    'change [role^="service-message-text"]' : 'updateServiceMessage',
    'click [role="save-service-message"]': 'saveServiceMessage'
  },

  initialize: function(){
    this.model.on('change', this.render, this);
    this.model.fetch();
  },

  render: function(){
    this.$el.find('[role="service-message-text1"]').val(this.model.get('message1') || "");
    this.$el.find('[role="service-message-text2"]').val(this.model.get('message2') || "");
    this.$el.find('[role="service-message-text3"]').val(this.model.get('message3') || "");
  },

  updateServiceMessage: function(e){
    this.model.set('message1', this.$el.find('[role="service-message-text1"]').val());
    this.model.set('message2', this.$el.find('[role="service-message-text2"]').val());
    this.model.set('message3', this.$el.find('[role="service-message-text3"]').val());
  },

  saveServiceMessage: function(){
    this.model.save();
  }
});


/* Coefficients */

var Coefficients = Backbone.Model.extend({
  url: '/coefficients',
  isNew: function(){ return false; }
});

var CoefficientsView = Backbone.View.extend({
  tagName: 'div',

  events: {
    'change [role="time-bonus"]'           : 'updateCoefficients',
    'change [role="friend-bonus"]'         : 'updateCoefficients',
    'change [role="free-base-score"]'      : 'updateCoefficients',
    'change [role="gold-base-score"]'      : 'updateCoefficients',
    'change [role="brilliant-base-score"]' : 'updateCoefficients',
    'change [role="silver1-base-score"]'   : 'updateCoefficients',
    'change [role="silver2-base-score"]'   : 'updateCoefficients',
    'click [role="save-coefficients"]'     : 'saveCoefficients'
  },

  initialize: function(){
    this.model.on('change', this.render, this);
    this.model.fetch();
  },

  render: function(){
    this.$el.find('[role="time-bonus"]').val(this.model.get("time-bonus") || "");
    this.$el.find('[role="friend-bonus"]').val(this.model.get("friend-bonus") || "");
    this.$el.find('[role="free-base-score"]').val(this.model.get("free-base-score") || "");
    this.$el.find('[role="gold-base-score"]').val(this.model.get("gold-base-score") || "");
    this.$el.find('[role="brilliant-base-score"]').val(this.model.get("brilliant-base-score") || "");
    this.$el.find('[role="silver1-base-score"]').val(this.model.get("silver1-base-score") || "");
    this.$el.find('[role="silver2-base-score"]').val(this.model.get("silver2-base-score") || "");
  },

  updateCoefficients: function(e){
    this.model.set("time-bonus", this.$el.find('[role="time-bonus"]').val());
    this.model.set("friend-bonus", this.$el.find('[role="friend-bonus"]').val());
    this.model.set("free-base-score", this.$el.find('[role="free-base-score"]').val());
    this.model.set("gold-base-score", this.$el.find('[role="gold-base-score"]').val());
    this.model.set("brilliant-base-score", this.$el.find('[role="brilliant-base-score"]').val());
    this.model.set("silver1-base-score", this.$el.find('[role="silver1-base-score"]').val());
    this.model.set("silver2-base-score", this.$el.find('[role="silver2-base-score"]').val());
  },

  saveCoefficients: function(){
    this.model.save();
  }
});


/* Notifications */

var NotificationsView = Backbone.View.extend({
  tagName: 'div',

  events: {
    'click [role="send-notification"]': 'sendNotification'
  },

  initialize: function(){
  },

  render: function(){
  },

  sendNotification: function(){
    var $notificationText = this.$el.find('[role="notification-text"]'),
        message = $notificationText.val();

    $notificationText.val('');
    Backbone.ajax({ data: { message: message }, type: 'POST', 'url': '/push_message' });
  }
});


/* Initializnig code */

var puzzleView, formSigninView, logoutView, puzzleSets, puzzleSetsView, users, usersView, dashboardView, serviceMessage, serviceMessageView, coefficients, coefficientsView, notificationsView;

$(function(){
  currentUser = new CurrentUser();
  formSigninView = new FormSigninView({ model: currentUser,
                                        el: $('[role="form-signin-container"]')[0] });
  logoutView = new LogoutView({ model: currentUser,
                                el: $('[role="logout"]')[0] });

  users = new Users;
  usersView = new UsersView({ el: $('[role="users"]')[0], collection: users });

  puzzleSets = new PuzzleSets();
  puzzleSetsView = new PuzzleSetsView({ el: $('[role="sets"]')[0], collection: puzzleSets});

  dashboardView = new DashboardView({ el: $('[role="dashboard"]')[0] });

  serviceMessageView = new ServiceMessageView({
                                                el: $('[role="service-message"]')[0],
                                                model: new ServiceMessage()
                                              });

  coefficientsView = new CoefficientsView({
                                            el: $('[role="coefficients"]')[0],
                                            model: new Coefficients()
                                          });

  notificationsView = new NotificationsView({
                                            el: $('[role="notifications"]')[0]
                                          });

  $('[role="loading-spinner"]').hide();
});
