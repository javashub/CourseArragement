<template>
  <div>
    <!-- footer -->
    <div style="margin-top: 50px"></div>
    <!-- footer -->
    <div class="wave-box">
      <div class="marquee-box marquee-up" id="marquee-box">
        <div class="marquee">
          <div class="wave-list-box" id="wave-list-box1">
            <ul>
              <li>
                <img height="60" alt="波浪" src="images/wave_02.png" />
              </li>
            </ul>
          </div>
          <div class="wave-list-box" id="wave-list-box2">
            <ul>
              <li>
                <img height="60" alt="波浪" src="images/wave_02.png" />
              </li>
            </ul>
          </div>
        </div>
      </div>
      <div class="marquee-box" id="marquee-box3">
        <div class="marquee">
          <div class="wave-list-box" id="wave-list-box4">
            <ul>
              <li>
                <img height="60" alt="波浪" src="images/wave_01.png" />
              </li>
            </ul>
          </div>
          <div class="wave-list-box" id="wave-list-box5">
            <ul>
              <li>
                <img height="60" alt="波浪" src="images/wave_01.png" />
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    <!-- 网站底部 -->
    <div class="footers">
      <div class="auto clearfix">
        <!-- footer主要-->
        <div class="five-superiority">
          <ul class="five-superiority-list clearfix">
            <li class="compensate_ico">
              <a href="#compensate">
                <span class="superiority-icon"></span>
                <span>学不会赔</span>
              </a>
            </li>
            <li class="retreat_ico">
              <a href="#refund">
                <span class="superiority-icon"></span>
                <span>5天成为Java架构师</span>
              </a>
            </li>
            <li class="technology_ico">
              <a class="superiority-text" href="#afterService">
                <span class="superiority-icon"></span>
                <span>7x24小时视频更新</span>
              </a>
            </li>
            <li class="prepare_ico">
              <a href="#ico">
                <span class="superiority-icon"></span>
                <span>999T学习资料</span>
              </a>
            </li>
            <li class="service_ico">
              <a href="#secret">
                <span class="superiority-icon"></span>
                <span>1V1校企合作包就业</span>
              </a>
            </li>
          </ul>
        </div>
        <div class="footer-floor1">
          <div class="footer-list">
            <ul>
              <li class="flist-title">产品与服务</li>
              <li>
                <a href="#">
                  <strong>企业服务</strong>
                </a>
              </li>
              <li>
                <a href="#">
                  <strong>人才招聘</strong>
                </a>
              </li>
              <li>
                <a href="#">
                  <strong>联系我们</strong>
                </a>
              </li>
              <li>
                <a href="#">
                  <strong>讲师招募</strong>
                </a>
              </li>
            </ul>
            <ul>
              <li class="flist-title">技术与支持</li>
              <li>
                <a href="#">意见反馈</a>
              </li>
              <li>
                <a href="#">帮助中心</a>
              </li>
            </ul>
            <ul class="flist-4">
              <li class="flist-title">关于在线网课</li>
              <li>
                <a href="#">加入我们</a>
              </li>
              <li>
                <a href="#">服务协议</a>
              </li>
              <li>
                <a href="#">友情链接</a>
              </li>
            </ul>
            <div class="clear-float"></div>
          </div>
          <div class="footer-right">
            <div class="telephone" title="服务热线">
              <span></span>
              <div class="tel-number">888-888-666</div>
            </div>
            <div class="official-plat">
              <p class="weixin" style="background-image: ">
                <span id="wx-corner"></span>
              </p>
              <p class="weibo" style="background-image: ">
                <span id="wb-corner"></span>
              </p>
              <ul>
                <li>
                  <a href="#a_null">
                    <span class="weixin-logo"></span>
                    官方微信
                  </a>
                </li>
                <li title="点击打开官方微博">
                  <a href="#" target="_blank">
                    <span class="weibo-logo"></span>
                    官方微博
                  </a>
                </li>
                <li title="点击打开官方客服">
                  <a
                    href="http://crm2.qq.com/page/portalpage/wpa.php?uin=XXXXXXXX&amp;aty=0&amp;a=0&amp;curl=&amp;ty=1"
                  >
                    <span class="qq-logo"></span>
                    官方客服
                  </a>
                </li>
              </ul>
            </div>
          </div>
          <div class="clear-float"></div>
        </div>
      </div>
      <div class="footer-floor2">
        <p>网站信息·2020-2100 版权所有</p>
        <p>Copyright(C) YiKe Liang, All Rights Reserved.</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "HomeFooter",
  data() {
    return {};
  },
  mounted() {},
  methods: {}
};
</script>

<style lang="less" scoped>

</style>