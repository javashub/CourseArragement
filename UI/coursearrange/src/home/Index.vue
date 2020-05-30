<template>
  <div>
    <!-- 头部 -->
    <div class="m-indextopwrap f-pr"> 
		   <div class="m-indextopnav"> 
		    <div class="g-flow"> 
		     <div class="topnav f-pr f-cb"> 
		      <div class="u-indexnavcatebtn"> 
		       <a href="#" @click="hide" class="cbtn f-cb"> 
		         <div class="ic f-fl"> 
		         </div> <span class="qb f-fl f-f0">全部精品课</span>
		       </a> 
		      </div> 
		      <div class="u-indexnavcatedialog f-pa" id="j-nav-indexcatedialog"> 
		      	<!--左侧菜单栏-->
		       <div class="f-fl cateleft f-pa" id="zuoce"> 
		        <div class="catebg f-pa"> 
		        </div> 
		        <div class="items f-pa" >       
		         <div class="item" :class="[courseCategoryList.length-1==index?'last':'']" v-for="(course,index) in courseCategoryList"> 
		          <div class="curbg"> 
		          </div> 
		          <div class="inn"> 
		           <p> <a href="#" class="f-f0 first">{{course.name}}</a> <a href="#" class="second">{{course.courseCategory[0].categoryList[0].name}}</a> <a href="#" class="second">{{course.courseCategory[0].categoryList[1].name}}</a> <a href="#" class="second">{{course.courseCategory[0].categoryList[2].name}}</a></p> 
		          </div> 
		         </div>                 
		        </div> 
		       </div> 
		       <!--左侧触发的菜单栏-->
		       <div class="cateright f-pa f-cb"> 
		        <a class="close f-pa j-close" onclick="$('.rwrap').hide();"></a>  
		        
		        <div class="f-pr rwrap f-cb cur" v-for="(course) in courseCategoryList"> 
		         <div class="rwrap-left f-cb"> 
		          <!--热门课程，可以遍历，但是没必要，看你自己的需求-->
		          <div class="top-rec-box f-cb"> 
		           <a class="top-rec f-fl" href="#">{{course.hotCourse[0].name}}</a> 
		           <a class="top-rec f-fl" href="#">{{course.hotCourse[1].name}}</a> 
		          </div> 
		          <div v-for="(courseCategory) in course.courseCategory">
		          	  <a href="#" class="f-f0 first cat2 tit f-fl">{{courseCategory.categoryName}}</a> 
			          <div class="links"> 
			           <p class="cate3links"> 
			           		<div v-for="(category,index) in courseCategory.categoryList" style="display: inline;">
			           			<span class="slash" v-if="index != courseCategory.categoryList.length">|</span>
			           			<a href="#" class="f-f0" :class="[index%2 != 0 ?'rec':'']">{{category.name}}</a>
			           		</div>
			           </p> 
			          </div> 
		          </div>
		         </div> 
		         <div class="rwrap-right f-cb"> 
		          <a class="f-f0 tit" href="#">系列课程</a> 
		          <p class="links"> 
		          	<div v-for="(seriesCourse) in course.seriesCourses" class="f-cb f-fc6 f-f0">
		          		<a href="#" >{{seriesCourse.name}}</a>          		
		          	</div>
		          	<a href="#" class="f-fc6 f-f0">更多&gt;&gt;</a> 
		          </p> 
		          <a href="#" class="picwrap"> <img :src="course.seriesImg" class="f-pa pic" /> </a> 
		         </div> 
		        </div>          
		       </div> 
		      
		      </div> 
		      <!--首页-->
		      <div class="mainnav f-cb j-navFind"> 
		       <a class="nitem f-f0" href="index.html" target="_self">首页</a> 
		       <div class="f-cb nitem f-f0 x-hoverItem"> 
		        <span>课程体系</span> 
		        <div class="f-pa u-navdropmenu x-child"> 
		         <span class="arrr f-pa"></span> 
		         <a class="f-f0 dropitem " href="#"> <span>IBM认知计算</span> </a> 
		         <a class="f-f0 dropitem " href="#"> <span>大学计算机专业</span> </a> 
		         <a class="f-f0 dropitem " href="#"> <span>互联网职业技能</span> </a> 
		         <a class="f-f0 dropitem last" href="#"> <span>职场提升课程体系</span> </a> 
		        </div> 
		       </div> 
			   <a class="nitem f-f0" href="onlineTest.html" target="_self">在线自测</a>
			   <a class="nitem f-f0" href="#" target="_self">后台管理</a> 
		      </div> 
		      <div class="search off f-cb"> 
		       <div class="box f-cb"> 
		        <input type="text" maxlength="30" placeholder="搜索课程" /> 
		       </div> 
		       <div class="submit f-hide f-pa"></div> 
		      </div> 
		     </div> 
		    </div> 
		   </div> 
  		</div>
      <!-- 头部结束 -->
        	<div id="m-slide-container"> 
		   <div> 
		   	<!--背景图轮播-->
		    <div class="m-slide f-pr"> 
		     <div class="f-pr m-slide-wrap" id="j-slidewrap"> 
		      <div class="slide" id="j-slide" href="#"> 
		       <ul> 
		        <li><a href=""><img src="" /></a></li> 
		        <li><a href=""><img src="" /></a></li> 
		        <li><a href=""><img src="" /></a></li> 
		        <li><a href=""><img src="" /></a></li> 
		        <li><a href=""><img src="" /></a></li> 
		        <li><a href=""><img src="" /></a></li> 
		        <li><a href=""><img src="" /></a></li> 
		       </ul> 
		      </div> 
		      <a class="lbtn prev"></a> 
		      <a class="rbtn next"></a> 
		      <div class="u-slidepg"> 
		       <ul class="f-cb f-pr f-ib pbox"> 
		        <li class="on">1</li> 
		        <li class="">2</li> 
		        <li class="">3</li> 
		        <li class="">4</li> 
		        <li class="">5</li> 
		        <li class="">6</li> 
		        <li class="">7</li> 
		       </ul> 
		      </div>  
		     </div> 
		     <div class="lbevel f-pa"> 
		     </div> 
		     <div class="rbevel f-pa"> 
		     </div> 
		    </div> 
		   </div> 
		  </div>
      
      <!--底部热门课程老师轮播-->
	 	<div class="m-micro" id="j-micro"> 
	   		<div class="g-flow f-pr"> 
		    	<div class="f-cb m-micro-wrap"> 
				     <div class="cntwrap" id="j-cntwrap"> 
				     	<!--热门课程-->
				      <div class="cntlist" id="j-cntlist"> 
				       <ul> 
				        <li class="item f-fl" v-for="(hotCourse) in hotCourseList" style="overflow: hidden;"> 
					        <a href="#"> 
					          <img :src="hotCourse.img" style=" border-radius: 50%; float: left;" class="imgbox" />					        		
					          <div class="tit f-f0 f-thide" style="display: inline;">
					            	{{hotCourse.name}} 
					          </div> 
					          <div class="tip f-f0 f-thide" style="display: inline;">
					            	{{hotCourse.des}}
					          </div> 
					        </a> 
				        </li>                            
				       </ul> 
				      </div> 
				      <div class="larr f-pa prev"> 
				      </div> 
				      <div class="rarr f-pa next"> 
				      </div> 
				     </div> 
		    	</div> 
	   		</div> 
  	  	</div> 

        <!--课程预览-->
  		<div class="main">
  			<ul class="breadcrumb">
				<li><a href="#">编程语言</a></li>
				<li class="active">Python</li>
			</ul>
			  <div class="dy-content">
			    <div class="dy-videos">
			      <ul class="dy-video-list">
			        <li data- class="dy-video-item" @click="openVideoDes(c.id)"  :class="[{'dy-video-meta-left dy-video-item-last':(index+1)%6==0},{'dy-video-meta-left':(index+1)%5==0},{'dy-video-meta-right':((index+1)%5!=0)&&((index+1)%6!=0)}]"  v-for="(c,index) in courseList">
			          <div class="dy-video-meta">
			            <div class="dy-video-meta-bg"> </div>
			            <div class="dy-video-meta-dy">
			              <h4 class="dy-video-title"> <a> {{c.name}} </a> </h4>
			              <span class="dy-video-rating">{{c.score}}</span>
			              <ul class="dy-video-meta-list">
			                <li class="dy-video-actors"> <span class="dy-video-tip"> 教师: </span> <a>{{c.teacherName}}</a></li>
			                <li class="dy-video-types"> <span class="dy-video-tip">类型:</span> <span class="dy-video-meta-filter">{{c.courseCategory}}</span> </li>
			                <li class="dy-video-starts"> <span class="dy-split">|</span> <span class="dy-video-tip">开课时间:</span> <span class="dy-video-meta-filter">{{c.createTime}}</span> </li>
			              </ul>
			              <p class="dy-video-intro"> {{c.des}} <a class="dy-video-intro-more">更多</a> </p>
			            </div>
			            <div class="dy-video-meta-bg"> </div>
			          </div>
			          <div class="dy-video-poster"> 
			          	<a class="dy-video-link"> <img class="dy-video-img" :src="c.img"> 
			          		<span class="dy-video-nocomplete"></span> <span class="dy-video-date"> {{c.createTime}} </span> 
			          		<span class="dy-video-bg"></span> <span class="s-pay"></span> 
			          	</a> 
			          </div>
			          <div class="dy-video-primary">
			            <h4 class="dy-video-title"><a> {{c.name}} </a> </h4>
			            <span class="dy-video-rating">{{c.score}} </span> 
					  </div>
			        </li>			        		        			       			       			               			       			      			       
			      </ul>
			    </div>
			  </div>
		</div>		
  </div>
</template>

<script>
export default {
  name: 'Index',
  data() {
    return {

    }
  },
  mounted() {

  },
  methods: {

  }
}
</script>

<style lang="less" scoped>

</style>