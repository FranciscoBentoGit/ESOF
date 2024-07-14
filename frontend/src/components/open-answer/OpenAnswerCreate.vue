<template>
  <div>
    <div class="open-answer-response">
      <v-row>
        <v-col cols="10">
          <v-textarea
            v-model="sQuestionDetails.response"
            :label="`Response`"
            :data-cy="`questionResponseTextArea`"
            rows="1"
            auto-grow
          ></v-textarea>
        </v-col>
      </v-row>

      <!-- v-on:change="handleInput" -->
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Model, PropSync, Vue, Watch } from 'vue-property-decorator';
import OpenAnswerQuestionDetails from '@/models/management/questions/OpenAnswerQuestionDetails';
@Component
export default class OpenAnswerCreate extends Vue {
  @PropSync('questionDetails', { type: OpenAnswerQuestionDetails })
  sQuestionDetails!: OpenAnswerQuestionDetails;
}
</script>
